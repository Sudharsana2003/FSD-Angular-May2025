// src/main/java/com/hexa/cozyhavenstay/service/impl/HotelServiceImpl.java
package com.hexa.cozyhavenstay.service.impl;

import com.hexa.cozyhavenstay.dto.HotelSearchResponseDto;
import com.hexa.cozyhavenstay.dto.HotelRoomsDetailsDto;
import com.hexa.cozyhavenstay.dto.RoomAvailabilityDto;
import com.hexa.cozyhavenstay.dto.HotelRequest;
import com.hexa.cozyhavenstay.dto.HotelDetailsDto;
import com.hexa.cozyhavenstay.model.BookedRoomDetail;
import com.hexa.cozyhavenstay.model.Hotel;
import com.hexa.cozyhavenstay.model.HotelAmenity;
import com.hexa.cozyhavenstay.model.Room;
import com.hexa.cozyhavenstay.model.RoomType;
import com.hexa.cozyhavenstay.model.User;
import com.hexa.cozyhavenstay.model.RoleType;
import com.hexa.cozyhavenstay.repository.BookedRoomDetailRepository;
import com.hexa.cozyhavenstay.repository.HotelRepository;
import com.hexa.cozyhavenstay.repository.ReviewRepository;
import com.hexa.cozyhavenstay.repository.RoomRepository;
import com.hexa.cozyhavenstay.repository.UserRepository;
import com.hexa.cozyhavenstay.service.HotelService;
import com.hexa.cozyhavenstay.exception.DuplicateEntryException;
import com.hexa.cozyhavenstay.exception.ResourceNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException; // Make sure this import is present
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final BookedRoomDetailRepository bookedRoomDetailRepository;
    private final UserRepository userRepository;

    public HotelServiceImpl(HotelRepository hotelRepository,
                            RoomRepository roomRepository,
                            ReviewRepository reviewRepository,
                            BookedRoomDetailRepository bookedRoomDetailRepository,
                            UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.reviewRepository = reviewRepository;
        this.bookedRoomDetailRepository = bookedRoomDetailRepository;
        this.userRepository = userRepository;
    }

    // --- Helper method to map Hotel entity to HotelDetailsDto ---
    private HotelDetailsDto mapToHotelDetailsDto(Hotel hotel) {
        HotelDetailsDto hotelDto = new HotelDetailsDto();
        hotelDto.setHotelId(hotel.getHotelId());
        hotelDto.setHotelName(hotel.getHotelName());
        hotelDto.setLocation(hotel.getLocation());
        hotelDto.setAddress(hotel.getAddress());
        hotelDto.setDescription(hotel.getDescription());
        hotelDto.setContactCountryCode(hotel.getContactCountryCode());
        hotelDto.setContactLocalPhoneNumber(hotel.getContactLocalPhoneNumber());
        hotelDto.setContactEmail(hotel.getContactEmail());
        hotelDto.setIsActive(hotel.getIsActive());

        if (hotel.getHotelAmenities() != null) {
            hotelDto.setAmenities(hotel.getHotelAmenities().stream()
                    .map(ha -> ha.getAmenity().getAmenityName())
                    .collect(Collectors.toList()));
        } else {
            hotelDto.setAmenities(new ArrayList<>());
        }

        reviewRepository.findAverageRatingByHotelId(hotel.getHotelId())
                .ifPresentOrElse(
                        hotelDto::setAverageRating,
                        () -> hotelDto.setAverageRating(0.0)
                );

        LocalDate defaultCheckIn = LocalDate.now();
        LocalDate defaultCheckOut = LocalDate.now().plusDays(1);
        roomRepository.findMinFareByHotelIdAndAvailability(
                hotel.getHotelId(), defaultCheckIn, defaultCheckOut)
                .ifPresentOrElse(
                        minFare -> hotelDto.setMinPricePerNight(minFare),
                        () -> hotelDto.setMinPricePerNight(0.0)
                );

        return hotelDto;
    }

    // --- CRUD methods (Updated for HotelRequest DTO and Owner context) ---

    @Override
    @Transactional
    public HotelDetailsDto createHotel(HotelRequest hotelRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User ownerUser = userRepository.findByUsername(username)
                                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user (owner) not found."));

        if (hotelRepository.findByHotelNameAndLocation(hotelRequest.getHotelName(), hotelRequest.getLocation()).isPresent()) {
            throw new DuplicateEntryException("A hotel with the same name and location already exists.");
        }

        Hotel hotel = new Hotel();
        hotel.setHotelName(hotelRequest.getHotelName());
        hotel.setLocation(hotelRequest.getLocation());
        hotel.setAddress(hotelRequest.getAddress());
        hotel.setDescription(hotelRequest.getDescription());
        hotel.setContactCountryCode(hotelRequest.getContactCountryCode());
        hotel.setContactLocalPhoneNumber(hotelRequest.getContactLocalPhoneNumber());
        hotel.setContactEmail(hotelRequest.getContactEmail());
        hotel.setIsActive(true);
        hotel.setOwnerUser(ownerUser);

        Hotel savedHotel = hotelRepository.save(hotel);
        return mapToHotelDetailsDto(savedHotel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HotelDetailsDto> getHotelById(Integer id) {
        return hotelRepository.findByIdWithAmenities(id)
                .map(this::mapToHotelDetailsDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDetailsDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::mapToHotelDetailsDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HotelDetailsDto updateHotel(Integer id, HotelRequest hotelRequest) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + id));

        // --- OWNERSHIP CHECK START (for update) ---
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));

        boolean isAdmin = RoleType.ADMIN.equals(currentUser.getRole());
        if (!isAdmin) {
            if (existingHotel.getOwnerUser() == null || !existingHotel.getOwnerUser().getUserId().equals(currentUser.getUserId())) {
                throw new AccessDeniedException("You are not authorized to update this hotel as you are not its owner.");
            }
        }
        // --- OWNERSHIP CHECK END ---

        Optional<Hotel> duplicateCheck = hotelRepository.findByHotelNameAndLocation(hotelRequest.getHotelName(), hotelRequest.getLocation());
        if (duplicateCheck.isPresent() && !duplicateCheck.get().getHotelId().equals(id)) {
            throw new DuplicateEntryException("Another hotel with the same name and location already exists.");
        }

        existingHotel.setHotelName(hotelRequest.getHotelName());
        existingHotel.setLocation(hotelRequest.getLocation());
        existingHotel.setAddress(hotelRequest.getAddress());
        existingHotel.setDescription(hotelRequest.getDescription());
        existingHotel.setContactCountryCode(hotelRequest.getContactCountryCode());
        existingHotel.setContactLocalPhoneNumber(hotelRequest.getContactLocalPhoneNumber());
        existingHotel.setContactEmail(hotelRequest.getContactEmail());
        if (hotelRequest.getIsActive() != null) {
            existingHotel.setIsActive(hotelRequest.getIsActive());
        }

        Hotel updatedHotel = hotelRepository.save(existingHotel);
        return mapToHotelDetailsDto(updatedHotel);
    }

    @Override
    @Transactional
    public void deleteHotel(Integer id) {
        Hotel existingHotel = hotelRepository.findById(id) // Fetch hotel to check ownership
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        // --- OWNERSHIP CHECK START (for delete) ---
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));

        boolean isAdmin = RoleType.ADMIN.equals(currentUser.getRole());
        if (!isAdmin) {
            if (existingHotel.getOwnerUser() == null || !existingHotel.getOwnerUser().getUserId().equals(currentUser.getUserId())) {
                throw new AccessDeniedException("You are not authorized to delete this hotel as you are not its owner.");
            }
        }
        // --- OWNERSHIP CHECK END ---

        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDetailsDto> getMyHotels() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User ownerUser = userRepository.findByUsername(username)
                                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user (owner) not found."));
        return hotelRepository.findByOwnerUser(ownerUser).stream()
                .map(this::mapToHotelDetailsDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelSearchResponseDto> searchHotels(
            String location,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer numRooms) {

        List<Hotel> foundHotels = hotelRepository.findAvailableHotelsByLocationAndDates(
                location, checkInDate, checkOutDate, numRooms);

        return foundHotels.stream().map(hotel -> {
            HotelSearchResponseDto dto = new HotelSearchResponseDto();
            dto.setId(hotel.getHotelId());
            dto.setName(hotel.getHotelName());
            dto.setLocation(hotel.getLocation());
            dto.setAddress(hotel.getAddress());
            dto.setDescription(hotel.getDescription());

            reviewRepository.findAverageRatingByHotelId(hotel.getHotelId())
                    .ifPresentOrElse(
                            avg -> dto.setRating(Math.round(avg * 10.0) / 10.0),
                            () -> dto.setRating(0.0)
                    );

            dto.setImageUrl("https://example.com/images/default_hotel_image.jpg");

            Integer totalRoomsInHotel = (int) roomRepository.countByHotelHotelId(hotel.getHotelId());

            long distinctBookedRooms = bookedRoomDetailRepository.findBookedRoomsForHotelAndDateRange(
                                                hotel.getHotelId(), checkInDate, checkOutDate)
                                                .stream()
                                                .map(brd -> brd.getRoom().getRoomId())
                                                .distinct()
                                                .count();

            dto.setAvailableRoomsCount(totalRoomsInHotel - (int) distinctBookedRooms);

            Optional<Double> minFareOptional = roomRepository.findMinFareByHotelIdAndAvailability(
                                                    hotel.getHotelId(), checkInDate, checkOutDate);
            dto.setMinFarePerNight(minFareOptional.orElse(0.0));

            if (hotel.getHotelAmenities() != null && !hotel.getHotelAmenities().isEmpty()) {
                dto.setAmenities(hotel.getHotelAmenities().stream()
                        .map(hotelAmenity -> hotelAmenity.getAmenity().getAmenityName())
                        .collect(Collectors.toList()));
            } else {
                dto.setAmenities(List.of());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HotelRoomsDetailsDto getHotelDetailsWithRoomAvailability(
            Integer hotelId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer numAdults,
            Integer numChildren) {

        Hotel hotel = hotelRepository.findByIdWithAmenities(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        if (!hotel.getIsActive()) {
            throw new ResourceNotFoundException("Hotel with ID: " + hotelId + " is not active.");
        }

        List<Room> allHotelRooms = roomRepository.findByHotelIdAndIsAvailable(hotelId);

        List<Integer> bookedRoomIds = bookedRoomDetailRepository.findBookedRoomsForHotelAndDateRange(
                hotelId, checkInDate, checkOutDate)
                .stream()
                .map(brd -> brd.getRoom().getRoomId())
                .distinct()
                .collect(Collectors.toList());

        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (numberOfNights <= 0) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        List<RoomAvailabilityDto> availableRooms = new ArrayList<>();
        Map<Integer, List<Room>> physicalRoomsByRoomType = allHotelRooms.stream()
                .collect(Collectors.groupingBy(room -> room.getRoomType().getRoomTypeId()));

        for (Map.Entry<Integer, List<Room>> entry : physicalRoomsByRoomType.entrySet()) {
            List<Room> roomsOfType = entry.getValue();
            RoomType roomType = roomsOfType.get(0).getRoomType();

            int roomsCurrentlyAvailableOfType = 0;
            Room representativeAvailableRoom = null;

            for (Room room : roomsOfType) {
                if (!bookedRoomIds.contains(room.getRoomId())) {
                    roomsCurrentlyAvailableOfType++;
                    if (representativeAvailableRoom == null) {
                        representativeAvailableRoom = room;
                    }
                }
            }

            if (roomsCurrentlyAvailableOfType > 0) {
                RoomAvailabilityDto roomDto = new RoomAvailabilityDto();
                roomDto.setRoomId(representativeAvailableRoom.getRoomId());
                roomDto.setRoomNumber(representativeAvailableRoom.getRoomNumber());
                roomDto.setRoomType(roomType.getTypeName());
                roomDto.setRoomDescription(roomType.getDescription());
                roomDto.setRoomSizeSqm(representativeAvailableRoom.getRoomSizeSqm());
                roomDto.setRoomSizeSqft(representativeAvailableRoom.getRoomSizeSqft());
                roomDto.setBedPreference(representativeAvailableRoom.getBedPreference());
                roomDto.setMaxPeople(representativeAvailableRoom.getMaxPeople());
                roomDto.setBaseFarePerNight(representativeAvailableRoom.getBaseFarePerNight());
                roomDto.setIsAc(representativeAvailableRoom.getIsAc());
                roomDto.setIsAvailable(true);
                roomDto.setAvailableCount(roomsCurrentlyAvailableOfType);

                BigDecimal totalFareForStay = calculateTotalFare(
                        representativeAvailableRoom.getBaseFarePerNight(),
                        roomType.getDefaultMaxAdults(),
                        roomType.getDefaultMaxChildren(),
                        numAdults,
                        numChildren,
                        numberOfNights,
                        representativeAvailableRoom.getBedPreference()
                );
                roomDto.setCalculatedTotalFareForStay(totalFareForStay);
                availableRooms.add(roomDto);
            }
        }

        HotelDetailsDto hotelDto = mapToHotelDetailsDto(hotel);
        return new HotelRoomsDetailsDto(hotelDto, availableRooms);
    }

    private BigDecimal calculateTotalFare(
            BigDecimal baseFarePerNight,
            Byte defaultMaxAdults,
            Byte defaultMaxChildren,
            Integer requestedAdults,
            Integer requestedChildren,
            long numberOfNights,
            String bedPreference) {

        BigDecimal dailyFare = baseFarePerNight;
        int totalRequestedPeople = requestedAdults + requestedChildren;

        BigDecimal adultExtraChargePercentage = new BigDecimal("0.40");
        BigDecimal childExtraChargePercentage = new BigDecimal("0.20");

        int maxPeopleWithoutExtraCharge;

        if (bedPreference != null) {
            String lowerBedPreference = bedPreference.toLowerCase();
            if (lowerBedPreference.contains("single")) {
                maxPeopleWithoutExtraCharge = 1;
            } else if (lowerBedPreference.contains("double") || lowerBedPreference.contains("king") || lowerBedPreference.contains("queen")) {
                maxPeopleWithoutExtraCharge = 2;
            } else {
                maxPeopleWithoutExtraCharge = (defaultMaxAdults != null ? defaultMaxAdults.intValue() : 0) +
                                              (defaultMaxChildren != null ? defaultMaxChildren.intValue() : 0);
            }
        } else {
            maxPeopleWithoutExtraCharge = (defaultMaxAdults != null ? defaultMaxAdults.intValue() : 0) +
                                          (defaultMaxChildren != null ? defaultMaxChildren.intValue() : 0);
        }

        if (maxPeopleWithoutExtraCharge <= 0) {
            maxPeopleWithoutExtraCharge = 2;
        }

        if (totalRequestedPeople > maxPeopleWithoutExtraCharge) {
            int chargeableAdults = Math.max(0, requestedAdults - (defaultMaxAdults != null ? defaultMaxAdults.intValue() : 0));
            int chargeableChildren = Math.max(0, requestedChildren - (defaultMaxChildren != null ? defaultMaxChildren.intValue() : 0));

            int totalChargeablePeople = chargeableAdults + chargeableChildren;
            int actualExtraPeople = totalRequestedPeople - maxPeopleWithoutExtraCharge;

            if (totalChargeablePeople > actualExtraPeople) {
                chargeableAdults = Math.min(chargeableAdults, actualExtraPeople);
                chargeableChildren = Math.max(0, actualExtraPeople - chargeableAdults);
            }

            BigDecimal extraChargeForAdults = BigDecimal.ZERO;
            if (chargeableAdults > 0) {
                extraChargeForAdults = baseFarePerNight.multiply(adultExtraChargePercentage)
                                                     .multiply(new BigDecimal(chargeableAdults));
            }

            BigDecimal extraChargeForChildren = BigDecimal.ZERO;
            if (chargeableChildren > 0) {
                extraChargeForChildren = baseFarePerNight.multiply(childExtraChargePercentage)
                                                         .multiply(new BigDecimal(chargeableChildren));
            }

            dailyFare = dailyFare.add(extraChargeForAdults).add(extraChargeForChildren);
        }

        return dailyFare.multiply(new BigDecimal(numberOfNights));
    }
}