package cn.shuzilm.backend.master;

import cn.shuzilm.util.db.Select;
import cn.shuzilm.util.db.Update;

/**
 * Created by thunders on 2018/7/11.
 */
public abstract class Service {

    protected Select select = new Select();
    protected Update update = new Update();

}
