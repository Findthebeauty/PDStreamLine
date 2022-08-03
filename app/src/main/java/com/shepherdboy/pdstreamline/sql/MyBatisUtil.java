package com.shepherdboy.pdstreamline.sql;

import com.shepherdboy.pdstreamline.MyApplication;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MyBatisUtil {

    private static SqlSessionFactory factory = null;

    static {

        try {

            String resource = "mybatis-config.xml";

            //加载MyBatis的主配置文件
//            InputStream inputStream = Resources.getResourceAsStream(resource);
            InputStream i = MyApplication.getContext().getResources().openRawResource(1);

            factory = new SqlSessionFactoryBuilder().build(i);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public static SqlSession getSqlSession() throws IOException {

        return factory.openSession();
    }

}
