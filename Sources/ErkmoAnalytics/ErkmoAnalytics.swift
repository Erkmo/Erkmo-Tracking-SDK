import Foundation
import UIKit

public final class ErkmoAnalytics {
    public static let shared = ErkmoAnalytics()

    private var siteId: String = ""
    private var apiUrl: String = "https://t.erkmo.com/track"
    private var sessionId: String = ""
    private let sdkVersion = "ios-0.1"

    private init() {}

    public func configure(siteId: String, apiUrl: String = "https://t.erkmo.com/track") {
        self.siteId = siteId
        self.apiUrl = apiUrl
        self.sessionId = ErkmoAnalytics.loadOrCreateSessionId()
    }

    public func track(event: String, properties: [String: Any] = [:]) {
        send(event: event, properties: properties, screenName: nil)
    }

    public func screen(_ name: String, properties: [String: Any] = [:]) {
        send(event: "screen_view", properties: properties, screenName: name)
    }

    private func send(event: String, properties: [String: Any], screenName: String?) {
        guard !siteId.isEmpty, let url = URL(string: apiUrl) else { return }

        var payload: [String: Any] = [
            "site_id": siteId,
            "event": event,
            "timestamp": Int(Date().timeIntervalSince1970 * 1000),
            "session_id": sessionId,
            "platform": "ios",
            "sdk_version": sdkVersion,
            "app_name": Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as? String ?? "iOS App",
            "app_id": Bundle.main.bundleIdentifier ?? "unknown",
            "app_version": Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "0.0.0",
            "app_build": Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String ?? "0",
            "device_id": UIDevice.current.identifierForVendor?.uuidString ?? "",
            "screen_name": screenName ?? ""
        ]

        payload["properties"] = properties

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try? JSONSerialization.data(withJSONObject: payload, options: [])

        URLSession.shared.dataTask(with: request).resume()
    }

    private static func loadOrCreateSessionId() -> String {
        let key = "erkmo_session_id"
        let defaults = UserDefaults.standard
        if let existing = defaults.string(forKey: key), !existing.isEmpty {
            return existing
        }
        let newId = "session_\(UUID().uuidString)"
        defaults.set(newId, forKey: key)
        return newId
    }
}
