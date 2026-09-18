package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        // 修复：原 date.isBefore(end) 会漏掉 end 当天
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 一次查询所有日期的营业额，避免 N+1
        List<Map<String, Object>> rows = orderMapper.sumByTimeBetween(beginTime, endTime, Orders.COMPLETED);
        Map<LocalDate, Double> turnoverMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            // MySQL SUM(DECIMAL) 返回 BigDecimal，用 Number 统一接收
            Number turnoverNum = (Number) row.get("turnover");
            turnoverMap.put(date, turnoverNum == null ? 0.0 : turnoverNum.doubleValue());
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            turnoverList.add(turnoverMap.getOrDefault(date, 0.0));
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }


    /**
     * @return
     * @Param("begin")
     * @Param("end")
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 一次查询所有日期的新增用户数，避免 N+1
        List<Map<String, Object>> rows = userMapper.countNewUserByDay(beginTime, endTime);
        Map<LocalDate, Integer> newUserMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            Number countNum = (Number) row.get("count");
            newUserMap.put(date, countNum == null ? 0 : countNum.intValue());
        }

        // 起始日期之前的总用户数（基线），后续每天累加即可
        Integer baseTotal = userMapper.countUserBefore(beginTime);
        int cumulative = baseTotal == null ? 0 : baseTotal;

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            int newUser = newUserMap.getOrDefault(date, 0);
            cumulative += newUser;
            newUserList.add(newUser);
            totalUserList.add(cumulative);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询每日总订单数（不传状态，查全部）和每日有效订单数（已完成）
        List<Map<String, Object>> allRows = orderMapper.countOrderByTimeBetween(beginTime, endTime, null);
        List<Map<String, Object>> validRows = orderMapper.countOrderByTimeBetween(beginTime, endTime, Orders.COMPLETED);
        Map<LocalDate, Integer> totalOrderMap = new HashMap<>();
        Map<LocalDate, Integer> validOrderMap = new HashMap<>();
        for (Map<String, Object> row : allRows) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            Number countNum = (Number) row.get("orderCount");
            totalOrderMap.put(date, countNum == null ? 0 : countNum.intValue());
        }
        for (Map<String, Object> row : validRows) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            Number countNum = (Number) row.get("orderCount");
            validOrderMap.put(date, countNum == null ? 0 : countNum.intValue());
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;
        // 遍历日期列表，填充两个列表并累加总数
        for (LocalDate date : dateList) {
            int dayTotal = totalOrderMap.getOrDefault(date, 0);
            int dayValid = validOrderMap.getOrDefault(date, 0);
            orderCountList.add(dayTotal);
            validOrderCountList.add(dayValid);
            totalOrderCount += dayTotal;
            validOrderCount += dayValid;
        }

        // 完成率 = 有效订单数 / 总订单数（防除零）
        double orderCompletionRate = totalOrderCount == 0 ? 0.0 : (double) validOrderCount / totalOrderCount;
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String nameList = StringUtils.join(names,",");
        String numberList = StringUtils.join(numbers,",");
        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}