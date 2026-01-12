package com.synkork.backend.controller;

import org.springframework.web.bind.annotation.RestController;

import com.synkork.backend.entity.User;
import com.synkork.backend.service.UserService;
import com.synkork.backend.utils.ApiResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Controller là NƠI XỬ LÝ CÁC YÊU CẦU HTTP TỪ CLIENT VÀ XỬ LÝ VỀ PHẦN TRẢ VỀ DỮ LIỆU
// Những phần về XỬ LÍ NGHIỆP VỤ sẽ được chuyển xuống SERVICE để tách biệt rõ ràng các tầng trong ứng dụng

// Mỗi controller đều được đánh dấu với @RestController để Spring biết đây là một controller
// Và mỗi controller nên có một BASE PATH riêng thông qua @RequestMapping (ví dụ: /users cho UserController)
// Có path riêng sẽ giúp tổ chức các endpoint rõ ràng hơn và làm security dễ dàng hơn
@RestController
@RequestMapping("/users")
public class UserController {

  // @Autowired sẽ giúp Spring tự động tiêm các bean tương ứng vào, đỡ phải ghi
  // code khởi tạo thủ công
  // Ở đây chúng ta tiêm UserService để controller có thể gọi các phương thức
  // nghiệp vụ
  @Autowired
  UserService userService;

  // Mỗi controller nếu có trả về dữ liệu nên trả về ResponseEntity để code rõ
  // ràng hơn về HTTP status

  // Trong ResponseEntity, chúng ta sẽ sử dụng một lớp ApiResponse để chuẩn hóa
  // cấu trúc dữ liệu trả về (status, message, data)

  // Ở đây, do có RestMapping("/users") ở trên rồi nên đường dẫn đầy đủ sẽ là:
  // http:localhost:8080/users/
  @GetMapping("/")
  public ResponseEntity<ApiResponse<List<User>>> findAll() {
    return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách người dùng thành công", userService.findAll()));
  }

  // Như cái dưới đây thì đường dẫn đầy đủ sẽ là:
  // http:localhost:8080/users/{userId}
  @GetMapping("/{userId}")
  public String getUserById(@RequestParam String userId) {
    return new String();
  }
}
