package com.example.moneytalk.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoom createChatRoom(Product product, User buyer, User seller) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller);
        return existingRoom.orElseGet(() -> {
            ChatRoom newRoom = ChatRoom.builder()
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .isClosed(false)
                .build();
            return chatRoomRepository.save(newRoom);
        });
    }

    @Transactional(readOnly = true)
    public Optional<ChatRoom> getChatRoom(Product product, User buyer, User seller) {
        return chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller);
    }
}
