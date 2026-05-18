package com.blog.service.impl;

import com.blog.service.IpLocationService;
import com.blog.utils.CommentClientInfoUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class IpLocationServiceImpl implements IpLocationService {

    private static final String XDB_PATH = "xdb/ip2region_v4.xdb";
    private static final String UNKNOWN_SEGMENT = "0";
    private static final String OUTER_NET_LABEL = "外网 IP";

    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();

    private volatile Searcher searcher;

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource(XDB_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            LongByteArray content = Searcher.loadContentFromInputStream(inputStream);
            this.searcher = Searcher.newWithBuffer(Version.IPv4, content);
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (searcher != null) {
            searcher.close();
        }
    }

    @Override
    public String resolveLocation(String ipAddress) {
        String localLabel = CommentClientInfoUtil.resolveLocationLabel(ipAddress);
        if (!OUTER_NET_LABEL.equals(localLabel)) {
            return localLabel;
        }

        if (StringUtils.isBlank(ipAddress) || searcher == null) {
            return localLabel;
        }

        return cache.computeIfAbsent(ipAddress, this::searchLocation);
    }

    private String searchLocation(String ipAddress) {
        try {
            String region = searcher.search(ipAddress);
            return parseProvince(region, ipAddress);
        } catch (Exception e) {
            return CommentClientInfoUtil.resolveLocationLabel(ipAddress);
        }
    }

    private String parseProvince(String region, String ipAddress) {
        if (StringUtils.isBlank(region)) {
            return CommentClientInfoUtil.resolveLocationLabel(ipAddress);
        }

        String[] parts = region.split("\\|");
        String province = getSegment(parts, 1);
        if (isUsable(province)) {
            return province;
        }

        String country = getSegment(parts, 0);
        if (isUsable(country)) {
            return country;
        }

        String city = getSegment(parts, 2);
        if (isUsable(city)) {
            return city;
        }

        return CommentClientInfoUtil.resolveLocationLabel(ipAddress);
    }

    private String getSegment(String[] parts, int index) {
        if (parts == null || index < 0 || index >= parts.length) {
            return null;
        }
        return StringUtils.trimToNull(parts[index]);
    }

    private boolean isUsable(String value) {
        return StringUtils.isNotBlank(value) && !UNKNOWN_SEGMENT.equals(value);
    }
}
