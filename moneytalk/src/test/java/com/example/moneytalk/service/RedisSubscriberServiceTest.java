package com.example.moneytalk.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import static org.mockito.Mockito.*;

class RedisSubscriberServiceTest {

    @Mock
    private RedisMessageListenerContainer container;

    @Mock
    private MessageListenerAdapter listenerAdapter;

    @InjectMocks
    private RedisSubscriberService redisSubscriberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void subscribeChatRoom_호출됨() {
        // given
        Long chatRoomId = 123L;
        String expectedTopicName = "chatroom:" + chatRoomId;

        // when
        redisSubscriberService.subscribeChatRoom(chatRoomId);

        // then
        ArgumentCaptor<ChannelTopic> topicCaptor = ArgumentCaptor.forClass(ChannelTopic.class);
        verify(container).addMessageListener(eq(listenerAdapter), topicCaptor.capture());
        assert topicCaptor.getValue().getTopic().equals(expectedTopicName);
    }
}
