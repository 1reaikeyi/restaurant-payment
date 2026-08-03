package start.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.PlanDetailService;
import service.PlanService;

@RestController
@RequestMapping("/user/plan")
public class PlanController {
    @Autowired
    private PlanService planService;
    @Autowired
    private PlanDetailService planDetailService;
}
