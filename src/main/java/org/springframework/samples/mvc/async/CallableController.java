package org.springframework.samples.mvc.async;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

@Controller
@RequestMapping("/async/callable")
public class CallableController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping("/response-body")
	public @ResponseBody Callable<String> callable() {
		System.out.println("Current Parent Thread name = " + Thread.currentThread().getName());
		Long beginParentTime = System.currentTimeMillis();
		logger.info("Invoke callable, beginParentTime = " + beginParentTime);
		//
		Callable<String> callable = new Callable<String>() {
			@Override
			public String call() throws Exception {
                System.out.println("Current Child Thread name = " + Thread.currentThread().getName());
                logger.info("Invoke callable, beginChildTime = " + System.currentTimeMillis());
				Thread.sleep(5000);
				return "Callable result";
			}
		};
		System.out.println("Invoke callable, parent thread cost time = "
				+ (System.currentTimeMillis() - beginParentTime));
		return callable;
	}

	@RequestMapping("/view")
	public Callable<String> callableWithView(final Model model) {

		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				Thread.sleep(2000);
				model.addAttribute("foo", "bar");
				model.addAttribute("fruit", "apple");
				return "views/html";
			}
		};
	}

	@RequestMapping("/exception")
	public @ResponseBody Callable<String> callableWithException(
			final @RequestParam(required=false, defaultValue="true") boolean handled) {

		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				Thread.sleep(2000);
				if (handled) {
					// see handleException method further below
					throw new IllegalStateException("Callable error");
				}
				else {
					throw new IllegalArgumentException("Callable error");
				}
			}
		};
	}

	@RequestMapping("/custom-timeout-handling")
	public @ResponseBody WebAsyncTask<String> callableWithCustomTimeoutHandling() {

		Callable<String> callable = new Callable<String>() {
			@Override
			public String call() throws Exception {
				Thread.sleep(2000);
				return "Callable result";
			}
		};

		return new WebAsyncTask<String>(1000, callable);
	}

	@ExceptionHandler
	@ResponseBody
	public String handleException(IllegalStateException ex) {
		return "Handled exception: " + ex.getMessage();
	}

}
