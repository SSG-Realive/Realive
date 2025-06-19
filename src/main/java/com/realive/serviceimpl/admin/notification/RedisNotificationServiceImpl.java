//package com.realive.serviceimpl.notification;
//
//import com.realive.domain.customer.Customer;
//import com.realive.repository.customer.CustomerRepository;
//import com.realive.service.notification.NotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class RedisNotificationServiceImpl implements NotificationService {
//
//    private final CustomerRepository customerRepository;
//    private final RedisTemplate<String, String> redisTemplate;
//
//    private static final String NOTIFICATION_CHANNEL = "auction:notifications:";
//    private static final String USER_NOTIFICATION_CHANNEL = "user:notifications:";
//
//    @Override
//    @Transactional
//    public void sendAuctionWinNotification(Integer customerId, Integer auctionId, Integer winningPrice) {
//        Customer customer = customerRepository.findById(customerId.longValue())
//            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
//
//        Map<String, String> notification = new HashMap<>();
//        notification.put("type", "AUCTION_WIN");
//        notification.put("auctionId", auctionId.toString());
//        notification.put("winningPrice", winningPrice.toString());
//        notification.put("message", String.format("축하합니다! %d원에 낙찰되었습니다.", winningPrice));
//
//        // Redis Pub/Sub으로 알림 발송
//        String channel = USER_NOTIFICATION_CHANNEL + customerId;
//        redisTemplate.convertAndSend(channel, notification.toString());
//
//        // 알림 내역 저장 (최근 100개)
//        String notificationKey = "user:notifications:history:" + customerId;
//        redisTemplate.opsForList().leftPush(notificationKey, notification.toString());
//        redisTemplate.opsForList().trim(notificationKey, 0, 99);
//
//        log.info("낙찰 알림 발송 - 고객ID: {}, 경매ID: {}, 낙찰가: {}",
//            customerId, auctionId, winningPrice);
//    }
//
//    @Override
//    @Transactional
//    public void sendAuctionEndNotification(Integer customerId, Integer auctionId, boolean isWinner) {
//        Customer customer = customerRepository.findById(customerId.longValue())
//            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
//
//        Map<String, String> notification = new HashMap<>();
//        notification.put("type", "AUCTION_END");
//        notification.put("auctionId", auctionId.toString());
//        notification.put("isWinner", String.valueOf(isWinner));
//        notification.put("message", isWinner ?
//            "경매가 종료되었습니다. 낙찰을 축하드립니다!" :
//            "경매가 종료되었습니다. 다음 기회를 기다려주세요.");
//
//        String channel = USER_NOTIFICATION_CHANNEL + customerId;
//        redisTemplate.convertAndSend(channel, notification.toString());
//
//        String notificationKey = "user:notifications:history:" + customerId;
//        redisTemplate.opsForList().leftPush(notificationKey, notification.toString());
//        redisTemplate.opsForList().trim(notificationKey, 0, 99);
//
//        log.info("경매 종료 알림 발송 - 고객ID: {}, 경매ID: {}, 낙찰여부: {}",
//            customerId, auctionId, isWinner);
//    }
//
//    @Override
//    @Transactional
//    public void sendBidOutbidNotification(Integer customerId, Integer auctionId, Integer currentPrice) {
//        Customer customer = customerRepository.findById(customerId.longValue())
//            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
//
//        Map<String, String> notification = new HashMap<>();
//        notification.put("type", "BID_OUTBID");
//        notification.put("auctionId", auctionId.toString());
//        notification.put("currentPrice", currentPrice.toString());
//        notification.put("message", String.format("다른 입찰자가 %d원으로 입찰했습니다.", currentPrice));
//
//        String channel = USER_NOTIFICATION_CHANNEL + customerId;
//        redisTemplate.convertAndSend(channel, notification.toString());
//
//        String notificationKey = "user:notifications:history:" + customerId;
//        redisTemplate.opsForList().leftPush(notificationKey, notification.toString());
//        redisTemplate.opsForList().trim(notificationKey, 0, 99);
//
//        log.info("입찰가 초과 알림 발송 - 고객ID: {}, 경매ID: {}, 현재가: {}",
//            customerId, auctionId, currentPrice);
//    }
//}