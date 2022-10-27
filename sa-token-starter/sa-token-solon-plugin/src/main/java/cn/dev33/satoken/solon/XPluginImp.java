package cn.dev33.satoken.solon;

import org.noear.solon.Solon;
import org.noear.solon.core.AopContext;
import org.noear.solon.core.Plugin;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.basic.SaBasicTemplate;
import cn.dev33.satoken.basic.SaBasicUtil;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.second.SaTokenSecondContextCreator;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.id.SaIdTemplate;
import cn.dev33.satoken.id.SaIdUtil;
import cn.dev33.satoken.json.SaJsonTemplate;
import cn.dev33.satoken.listener.SaTokenEventCenter;
import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.same.SaSameTemplate;
import cn.dev33.satoken.sign.SaSignTemplate;
import cn.dev33.satoken.solon.model.SaContextForSolon;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempInterface;

/**
 * @author noear
 * @since 1.4
 */
@SuppressWarnings("deprecation")
public class XPluginImp implements Plugin {

    @Override
    public void start(AopContext context) {
        //集成初始化

        // 注入上下文Bean
        SaManager.setSaTokenContext(new SaContextForSolon());

        //注入配置Bean
        SaTokenConfig saTokenConfig = Solon.cfg().getBean("sa-token", SaTokenConfig.class);
        SaManager.setConfig(saTokenConfig);

        context.getBeanAsyn(SaTokenConfig.class, bean -> {
            SaManager.setConfig(bean);
        });


        // 注入Dao Bean
        context.getBeanAsyn(SaTokenDao.class, bean -> {
            SaManager.setSaTokenDao(bean);
        });

        // 注入二级上下文 Bean
        context.getBeanAsyn(SaTokenSecondContextCreator.class, bean -> {
            SaManager.setSaTokenSecondContext(bean.create());
        });

        // 注入侦听器 Bean
        context.subBean(SaTokenListener.class, sl -> {
            SaTokenEventCenter.registerListener(sl);
        });


        // 注入权限认证 Bean
        context.getBeanAsyn(StpInterface.class, bean -> {
            SaManager.setStpInterface(bean);
        });

        // 注入持久化 Bean
        context.getBeanAsyn(SaTokenDao.class, bean -> {
            SaManager.setSaTokenDao(bean);
        });

        // 临时令牌验证模块 Bean
        context.getBeanAsyn(SaTempInterface.class, bean -> {
            SaManager.setSaTemp(bean);
        });

        // Sa-Token-Id 身份凭证模块 Bean
        context.getBeanAsyn(SaIdTemplate.class, bean -> {
            SaIdUtil.saIdTemplate = bean;
        });

        // Sa-Token Same-Token 模块 Bean
        context.getBeanAsyn(SaSameTemplate.class, bean -> {
            SaManager.setSaSameTemplate(bean);
        });

        // Sa-Token Http Basic 认证模块 Bean
        context.getBeanAsyn(SaBasicTemplate.class, bean -> {
            SaBasicUtil.saBasicTemplate = bean;
        });

        // Sa-Token JSON 转换器 Bean
        context.getBeanAsyn(SaJsonTemplate.class, bean -> {
            SaManager.setSaJsonTemplate(bean);
        });

        // Sa-Token 参数签名算法 Bean
        context.getBeanAsyn(SaSignTemplate.class, bean -> {
            SaManager.setSaSignTemplate(bean);
        });

        // 自定义 StpLogic 对象
        context.getBeanAsyn(StpLogic.class, bean -> {
            StpUtil.setStpLogic(bean);
        });
    }
}