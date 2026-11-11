package com.mycompany.deviceservice.repository;


import com.mycompany.deviceservice.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByUserId(Long userId);


    void deleteByUserId(Long userId);


}