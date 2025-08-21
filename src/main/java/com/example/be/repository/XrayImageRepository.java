package com.example.be.repository;

import com.example.be.entity.XrayImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XrayImageRepository extends JpaRepository<XrayImage, Integer> {
}

