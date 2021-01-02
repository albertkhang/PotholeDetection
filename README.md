# PotholeDetection

**Nội dung**
- [PotholeDetection](#potholedetection)
  - [1. Chức năng](#1-chức-năng)
  - [2. Video Demo](#2-video-demo)
  - [3. Design](#3-design)
  - [4. Yêu cầu thiết bị](#4-yêu-cầu-thiết-bị)
  - [5. Thư viện](#5-thư-viện)
  - [6. Backend/Database](#6-backenddatabase)
  - [7. Thuật toán](#7-thuật-toán)
  - [8. Tác giả](#8-tác-giả)
  - [9. Giấy phép](#9-giấy-phép)

## 1. Chức năng
1. Đăng nhập bằng tài khoản Google.
2. Tự động ghi nhận/xử lý/upload lên server những đoạn đường xấu trong quá trình di chuyển (yêu cầu phải chạy app lần đầu tiên để ứng dụng chạy nền).
3. Hiển thị vị trí của hiện tại của người dùng.
## 2. Video Demo
Xem video demo ở [đây](/WorkInProgress/video/video-demo.mp4).
## 3. Design
Dự trên wireframe [Wires](https://www.behance.net/gallery/55462459/Wires-wireframe-kits-for-Adobe-XD).
## 4. Yêu cầu thiết bị
**Minimum Version**
```
Android API level: 21
Version: 5
Name: Lollipop
```
**Maximum Version**
```
Android API level: 30
Version: 11
Name: Android R
```
## 5. Thư viện
1. [Glide](https://github.com/bumptech/glide)
2. [Retrofit](https://github.com/square/retrofit)
3. [Paper](https://github.com/pilgr/Paper)
4. [Firebase](https://firebase.google.com/docs/android/setup?authuser=0)
## 6. Backend/Database
1. Sử dụng Cloud Firestore và Realtime Database của Firebase để lưu trữ dữ liệu.
2. Sử dụng Functions của Firebase để xử lý dữ liệu đoạn đường xấu được gửi từ người dùng.
## 7. Thuật toán
Thuật toán phát hiện đoạn đường xấu dựa trên sự thay đổi của các sensor trong điện thoại, được mô tả chi tiết trong phương pháp luận của [David Jackson](https://github.com/David-Jackson) trong repository [RoadQuality](https://github.com/David-Jackson/RoadQuality/blob/master/METHODOLOGY.md).
## 8. Tác giả
1. Tác giả của ứng dụng: Lê Đình Khang - 17520612 - Trường Đại học Công nghệ Thông tin - Đại học Quốc gia Thành phố Hồ Chí Minh, Việt Nam.
2. Tác giả của thuật toán: [David Jackson](https://github.com/David-Jackson).
## 9. Giấy phép
[MIT License](/LICENSE.md)