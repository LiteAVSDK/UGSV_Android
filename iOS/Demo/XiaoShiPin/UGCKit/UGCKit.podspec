#
# Be sure to run `pod lib lint UGCKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'UGCKit'
  s.version          = '0.1.0'
  s.summary          = 'A short description of UGCKit.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/originleeli@tencent.com/UGCKit'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'originleeli@tencent.com' => 'originleeli@tencent.com' }
  s.source           = { :git => 'https://github.com/originleeli@tencent.com/UGCKit.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'
  s.ios.deployment_target = '9.0'
  s.static_framework = true
  s.default_subspec = 'Professional'
  s.ios.framework    = ['SystemConfiguration','CoreTelephony', 'VideoToolbox', 'CoreGraphics', 'AVFoundation', 'Accelerate','AssetsLibrary']
  s.ios.library = 'z', 'resolv', 'iconv', 'stdc++', 'c++', 'sqlite3'

  s.subspec "Professional" do |ss|
    ss.resources      = 'UGCKit/Assets/**/*'
    ss.source_files = 'UGCKit/Classes/**/*.{h,m}'
    framework_path="../../../SDK/TXLiteAVSDK_Professional.framework"
    ss.pod_target_xcconfig={
        'HEADER_SEARCH_PATHS'=> [
          "$(PODS_TARGET_SRCROOT)/#{framework_path}/Headers",
          "$(PODS_TARGET_SRCROOT)/../../../SDK/TXLiteAVSDK_UGC.framework/Headers"
        ]
    }
    ss.resource_bundles = {
      'UGCKitResources' => ['UGCKit/Localizable/**/*','UGCKit/Assets/**/*.{png,xcassets,bundle,storyboard,xib}']
    }
  end

  s.subspec "UGC" do |ss|
    ss.resources      = 'UGCKit/Assets/**/*'
    ss.source_files   = 'UGCKit/Classes/**/*.{h,m}'
    framework_path="../../../SDK/TXLiteAVSDK_UGC.framework"
    ss.pod_target_xcconfig = {
        'HEADER_SEARCH_PATHS' => ["$(PODS_TARGET_SRCROOT)/#{framework_path}/Headers"]
    }
    ss.resource_bundles = {
      'UGCKitResources' => ['UGCKit/Localizable/**/*','UGCKit/Assets/**/*.{png,xcassets,bundle,storyboard,xib}']
    }
  end
  s.dependency 'BeautySettingKit'
  s.dependency 'xmagickit'
end
