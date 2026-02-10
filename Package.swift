// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "ErkmoAnalytics",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: "ErkmoAnalytics",
            targets: ["ErkmoAnalytics"]
        )
    ],
    targets: [
        .target(
            name: "ErkmoAnalytics",
            path: "Sources/ErkmoAnalytics"
        )
    ]
)
