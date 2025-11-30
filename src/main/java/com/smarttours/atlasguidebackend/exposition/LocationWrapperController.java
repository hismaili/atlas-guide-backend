package com.smarttours.atlasguidebackend.exposition;

import com.smarttours.atlasguidebackend.domain.location.GeoLocation;
import com.smarttours.atlasguidebackend.domain.service.LocationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationWrapperController {

    private final LocationService locationService;

    public LocationWrapperController(LocationService locationService) {
        this.locationService = locationService;
    }

    public List<GeoLocation> autoCompleteLocation(String query) {
        return locationService.autoCompleteLocation(query);
    }
}
