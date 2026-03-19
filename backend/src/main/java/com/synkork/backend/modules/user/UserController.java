package com.synkork.backend.modules.user;

import com.synkork.backend.modules.user.dto.UserInfoDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

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

  // Ở đây, do có RestMapping("/users") ở trên rồi nên đường dẫn đầy đủ sẽ là:
  // http:localhost:8080/api/users (TẠI SAO CÓ /api/ THÌ XEM Ở file application.yml , Tao cấu hình cho nó có dòng /api như thế đấy)
  @GetMapping
  public ResponseEntity<List<UserEntity>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }

  @GetMapping("/me")
  public ResponseEntity<UserInfoDto> getUserInfo(Authentication auth) {
    String username = auth.getName();
    System.out.println(username);
    UserInfoDto user = userService.getUserInfo(username);

    return ResponseEntity.ok(user);
  }
  // Như cái dưới đây thì đường dẫn đầy đủ sẽ là:
  // http:localhost:8080/api/users/{userId}
  @GetMapping("/{userId}")
  public String getUserById(@RequestParam String userId) {
    return new String();
  }
}
