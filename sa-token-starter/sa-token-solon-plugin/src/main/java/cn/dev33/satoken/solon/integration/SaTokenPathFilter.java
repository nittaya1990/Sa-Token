package cn.dev33.satoken.solon.integration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.noear.solon.Solon;
import org.noear.solon.core.handle.Action;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.noear.solon.core.handle.Handler;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.filter.SaFilterErrorStrategy;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.strategy.SaStrategy;

/**
 * sa-token 基于路由的过滤式鉴权（增加了注解的处理）；使用优先级要低些
 *
 * @author noear
 * @since 1.10
 */
public class SaTokenPathFilter implements Filter {
	/**
	 * 是否打开注解鉴权
	 */
	public boolean isAnnotation = true;

	// ------------------------ 设置此过滤器 拦截 & 放行 的路由

	/**
	 * 拦截路由
	 */
	protected List<String> includeList = new ArrayList<>();

	/**
	 * 放行路由
	 */
	protected List<String> excludeList = new ArrayList<>();

	/**
	 * 添加 [拦截路由]
	 *
	 * @param paths 路由
	 * @return 对象自身
	 */
	public SaTokenPathFilter addInclude(String... paths) {
		includeList.addAll(Arrays.asList(paths));
		return this;
	}

	/**
	 * 添加 [放行路由]
	 *
	 * @param paths 路由
	 * @return 对象自身
	 */
	public SaTokenPathFilter addExclude(String... paths) {
		excludeList.addAll(Arrays.asList(paths));
		return this;
	}

	/**
	 * 写入 [拦截路由] 集合
	 *
	 * @param pathList 路由集合
	 * @return 对象自身
	 */
	public SaTokenPathFilter setIncludeList(List<String> pathList) {
		includeList = pathList;
		return this;
	}

	/**
	 * 写入 [放行路由] 集合
	 *
	 * @param pathList 路由集合
	 * @return 对象自身
	 */
	public SaTokenPathFilter setExcludeList(List<String> pathList) {
		excludeList = pathList;
		return this;
	}

	/**
	 * 获取 [拦截路由] 集合
	 *
	 * @return see note
	 */
	public List<String> getIncludeList() {
		return includeList;
	}

	/**
	 * 获取 [放行路由] 集合
	 *
	 * @return see note
	 */
	public List<String> getExcludeList() {
		return excludeList;
	}


	// ------------------------ 钩子函数

	/**
	 * 认证函数：每次请求执行
	 */
	protected SaFilterAuthStrategy auth = r -> {
	};

	/**
	 * 异常处理函数：每次[认证函数]发生异常时执行此函数
	 */
	protected SaFilterErrorStrategy error = e -> {
		if (e instanceof SaTokenException) {
			throw (SaTokenException) e;
		} else {
			throw new SaTokenException(e);
		}
	};

	/**
	 * 前置函数：在每次[认证函数]之前执行
	 */
	protected SaFilterAuthStrategy beforeAuth = r -> {
	};

	/**
	 * 写入[认证函数]: 每次请求执行
	 *
	 * @param auth see note
	 * @return 对象自身
	 */
	public SaTokenPathFilter setAuth(SaFilterAuthStrategy auth) {
		this.auth = auth;
		return this;
	}

	/**
	 * 写入[异常处理函数]：每次[认证函数]发生异常时执行此函数
	 *
	 * @param error see note
	 * @return 对象自身
	 */
	public SaTokenPathFilter setError(SaFilterErrorStrategy error) {
		this.error = error;
		return this;
	}

	/**
	 * 写入[前置函数]：在每次[认证函数]之前执行
	 *
	 * @param beforeAuth see note
	 * @return 对象自身
	 */
	public SaTokenPathFilter setBeforeAuth(SaFilterAuthStrategy beforeAuth) {
		this.beforeAuth = beforeAuth;
		return this;
	}


	@Override
	public void doFilter(Context ctx, FilterChain chain) throws Throwable {
		try {
			//查找当前主处理
			Handler mainHandler = Solon.app().router().matchMain(ctx);

			//如果是静态文件，则不处理（静态文件，不在路由中）
			if (mainHandler != null) {
				Action action = (mainHandler instanceof Action ? (Action) mainHandler : null);

				if (isAnnotation && action != null) {
					// 获取此请求对应的 Method 处理函数
					Method method = action.method().getMethod();

					// 如果此 Method 或其所属 Class 标注了 @SaIgnore，则忽略掉鉴权
					if (SaStrategy.me.isAnnotationPresent.apply(method, SaIgnore.class)) {
						return;
					}

					// 注解校验
					SaStrategy.me.checkMethodAnnotation.accept(method);
				}

				//路径规则处理
				SaRouter.match(includeList).notMatch(excludeList).check(r -> {
					beforeAuth.run(mainHandler);
					auth.run(mainHandler);
				});
			}
		} catch (StopMatchException e) {

		} catch (SaTokenException e) {
			// 1. 获取异常处理策略结果
			Object result;
			if (e instanceof BackResultException) {
				result = e.getMessage();
			} else {
				result = error.run(e);
			}

			// 2. 写入输出流
			if (result != null) {
				ctx.render(result);
			}
			ctx.setHandled(true);
			return;
		}

		chain.doFilter(ctx);
	}
}
