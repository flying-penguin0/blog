package com.blog.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Resolve comment client metadata from request headers.
 */
public final class CommentClientInfoUtil {

    private static final int MAX_USER_AGENT_LENGTH = 255;
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");

    private CommentClientInfoUtil() {
    }

    public static CommentClientInfo resolve(HttpServletRequest request) {
        String ipAddress = resolveClientIp(request);
        String userAgent = sanitizeUserAgent(request.getHeader("User-Agent"));

        return new CommentClientInfo(
                ipAddress,
                userAgent,
                detectBrowser(userAgent),
                detectOperatingSystem(userAgent)
        );
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_CLIENT_IP",
                "X-Real-IP"
        };

        for (String headerName : headerNames) {
            String headerValue = request.getHeader(headerName);
            if (StringUtils.isBlank(headerValue) || "unknown".equalsIgnoreCase(headerValue)) {
                continue;
            }

            String ip = headerValue.split(",")[0].trim();
            if (StringUtils.isNotBlank(ip)) {
                return normalizeIp(ip);
            }
        }

        return normalizeIp(request.getRemoteAddr());
    }

    private static String normalizeIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return "Unknown IP";
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    public static String resolveLocationLabel(String ip) {
        if (StringUtils.isBlank(ip) || "Unknown IP".equalsIgnoreCase(ip)) {
            return "未知地区";
        }
        if ("127.0.0.1".equals(ip) || "::1".equals(ip)) {
            return "本地开发环境";
        }
        if (isPrivateIpv4(ip)) {
            return "局域网";
        }
        return "外网 IP";
    }

    private static boolean isPrivateIpv4(String ip) {
        if (!IPV4_PATTERN.matcher(ip).matches()) {
            return false;
        }

        String[] segments = ip.split("\\.");
        int first = Integer.parseInt(segments[0]);
        int second = Integer.parseInt(segments[1]);

        if (first == 10) {
            return true;
        }
        if (first == 172 && second >= 16 && second <= 31) {
            return true;
        }
        if (first == 192 && second == 168) {
            return true;
        }
        return first == 169 && second == 254;
    }

    private static String sanitizeUserAgent(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return "Unknown";
        }
        return StringUtils.left(userAgent.trim(), MAX_USER_AGENT_LENGTH);
    }

    private static String detectBrowser(String userAgent) {
        String ua = StringUtils.defaultString(userAgent).toLowerCase();
        if (ua.contains("edg/")) {
            return "Microsoft Edge";
        }
        if (ua.contains("opr/") || ua.contains("opera")) {
            return "Opera";
        }
        if (ua.contains("chrome/") && !ua.contains("edg/")) {
            return "Google Chrome";
        }
        if (ua.contains("firefox/")) {
            return "Mozilla Firefox";
        }
        if (ua.contains("safari/") && !ua.contains("chrome/")) {
            return "Safari";
        }
        if (ua.contains("msie") || ua.contains("trident/")) {
            return "Internet Explorer";
        }
        return "Unknown Browser";
    }

    public static String detectBrowserVersion(String userAgent) {
        String ua = StringUtils.defaultString(userAgent);
        String lowerUa = ua.toLowerCase();
        if (lowerUa.contains("edg/")) {
            return extractVersion(ua, "Edg/");
        }
        if (lowerUa.contains("opr/")) {
            return extractVersion(ua, "OPR/");
        }
        if (lowerUa.contains("opera/")) {
            return extractVersion(ua, "Opera/");
        }
        if (lowerUa.contains("chrome/") && !lowerUa.contains("edg/")) {
            return extractVersion(ua, "Chrome/");
        }
        if (lowerUa.contains("firefox/")) {
            return extractVersion(ua, "Firefox/");
        }
        if (lowerUa.contains("version/") && lowerUa.contains("safari/") && !lowerUa.contains("chrome/")) {
            return extractVersion(ua, "Version/");
        }
        if (lowerUa.contains("msie ")) {
            return extractVersion(ua, "MSIE ");
        }
        if (lowerUa.contains("rv:") && lowerUa.contains("trident/")) {
            return extractVersion(ua, "rv:");
        }
        return null;
    }

    private static String detectOperatingSystem(String userAgent) {
        String ua = StringUtils.defaultString(userAgent).toLowerCase();
        if (ua.contains("windows nt")) {
            return "Windows";
        }
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("mac os x") || ua.contains("macintosh")) {
            return "macOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "Unknown OS";
    }

    public static String detectOperatingSystemVersion(String userAgent) {
        String ua = StringUtils.defaultString(userAgent);
        String lowerUa = ua.toLowerCase();
        if (lowerUa.contains("windows nt")) {
            return mapWindowsVersion(extractVersion(ua, "Windows NT "));
        }
        if (lowerUa.contains("iphone os ")) {
            return normalizeVersion(extractVersion(ua, "iPhone OS "));
        }
        if (lowerUa.contains("cpu os ")) {
            return normalizeVersion(extractVersion(ua, "CPU OS "));
        }
        if (lowerUa.contains("android ")) {
            return normalizeVersion(extractVersion(ua, "Android "));
        }
        if (lowerUa.contains("mac os x ")) {
            return normalizeVersion(extractVersion(ua, "Mac OS X "));
        }
        return null;
    }

    private static String extractVersion(String userAgent, String marker) {
        int start = userAgent.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = start;
        while (end < userAgent.length()) {
            char ch = userAgent.charAt(end);
            if (Character.isDigit(ch) || ch == '.' || ch == '_') {
                end++;
                continue;
            }
            break;
        }
        if (end <= start) {
            return null;
        }
        return normalizeVersion(userAgent.substring(start, end));
    }

    private static String normalizeVersion(String version) {
        if (StringUtils.isBlank(version)) {
            return null;
        }
        return version.trim().replace('_', '.');
    }

    private static String mapWindowsVersion(String version) {
        if (StringUtils.isBlank(version)) {
            return null;
        }
        return switch (version) {
            case "10.0" -> null;
            case "6.3" -> "8.1";
            case "6.2" -> "8";
            case "6.1" -> "7";
            default -> version;
        };
    }

    public record CommentClientInfo(
            String ipAddress,
            String userAgent,
            String browser,
            String operatingSystem
    ) {
    }
}
