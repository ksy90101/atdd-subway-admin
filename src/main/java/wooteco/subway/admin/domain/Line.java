package wooteco.subway.admin.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.annotation.Id;

public class Line {
	@Id
	private Long id;
	private String name;
	private LocalTime startTime;
	private LocalTime endTime;
	private int intervalTime;
	private String color;
	private Set<LineStation> stations;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Line() {
	}

	public Line(Long id, String name, LocalTime startTime, LocalTime endTime, int intervalTime, String color) {
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.intervalTime = intervalTime;
		this.color = color;
		this.stations = new LinkedHashSet<>();
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public Line(String name, LocalTime startTime, LocalTime endTime, int intervalTime, String color) {
		this(null, name, startTime, endTime, intervalTime, color);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public int getIntervalTime() {
		return intervalTime;
	}

	public String getColor() {
		return color;
	}

	public Set<LineStation> getStations() {
		return stations;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void update(Line line) {
		if (line.getName() != null) {
			this.name = line.getName();
		}
		if (line.getStartTime() != null) {
			this.startTime = line.getStartTime();
		}
		if (line.getEndTime() != null) {
			this.endTime = line.getEndTime();
		}
		if (line.getIntervalTime() != 0) {
			this.intervalTime = line.getIntervalTime();
		}
		if (!line.getStations().isEmpty()) {
			this.stations = line.getStations();
		}
		if (line.getColor() != null) {
			this.color = line.getColor();
		}

		this.updatedAt = LocalDateTime.now();
	}

	public void addLineStation(LineStation lineStation) {
		if (!stations.isEmpty() && lineStation.getPreStationId() == null) {
			LineStation firstStation = getFirstLineStation("노선 시작점이 없습니다.");
			firstStation.updatePreLineStation(lineStation.getStationId());
		} else if (!stations.isEmpty() && isSamePreStation(lineStation)) {
			LineStation previousLineStation = getPreviousLineStation(lineStation);
			previousLineStation.updatePreLineStation(lineStation.getStationId());
		}

		stations.add(lineStation);
	}

	private LineStation getPreviousLineStation(LineStation lineStation) {
		return stations.stream()
			.filter(eachLineStation -> eachLineStation.getPreStationId() != null && lineStation.getPreStationId()
				.equals(eachLineStation.getPreStationId()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("잘못된 역명입니다."));
	}

	private LineStation getFirstLineStation(String errorMessage) {
		return stations.stream()
			.filter(eachLineStation -> eachLineStation.getPreStationId() == null)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(errorMessage));
	}

	private boolean isSamePreStation(LineStation lineStation) {
		return stations.stream()
			.anyMatch(eachLineStation -> eachLineStation.getPreStationId() != null &&
				eachLineStation.getPreStationId()
					.equals(lineStation.getPreStationId()));
	}

	public void removeLineStationById(Long stationId) {
		LineStation previousLineStation = stations.stream()
			.filter(station -> stationId.equals(station.getStationId()))
			.findFirst()
			.orElse(null);
		LineStation nextLineStation = stations.stream()
			.filter(station -> stationId.equals(station.getPreStationId()))
			.findFirst()
			.orElse(null);

		if (nextLineStation != null) {
			nextLineStation.updatePreLineStation(previousLineStation.getPreStationId());
		}

		stations.remove(previousLineStation);
	}

	public List<Long> getLineStationsId() {
		List<Long> stationIds = new ArrayList<>();

		if (stations.isEmpty()) {
			return stationIds;
		}

		LineStation firstLineStation = getFirstLineStation("노선 역 경로를 찾을 수 없습니다.");

		stationIds.add(firstLineStation.getStationId());

		while (true) {
			Long lastStationId = stationIds.get(stationIds.size() - 1);

			Optional<LineStation> nextLineStation = stations.stream()
				.filter(lineStation -> Objects.equals(lineStation.getPreStationId(), lastStationId))
				.findFirst();

			if (!nextLineStation.isPresent()) {
				break;
			}

			stationIds.add(nextLineStation.get().getStationId());
		}

		System.out.println();
		return stationIds;
	}
}