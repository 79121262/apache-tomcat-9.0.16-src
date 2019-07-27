/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.tomcat.util.digester;


import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;


/**
 * <p>Rule implementation that sets properties on the object at the top of the
 * stack, based on attributes with corresponding names.</p>
 */

public class SetPropertiesRule extends Rule {

    /**
     * Process the beginning of this element.
     *
     * @param namespace the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param theName the local name if the parser is namespace aware, or just
     *   the element name otherwise
     * @param attributes The attribute list for this element
     */
    @Override
    public void begin(String namespace, String theName, Attributes attributes)
            throws Exception {
        //与之对应的是在ObjectCreateRule类的 begin(Attributes attributes)方法中会执行digester.push(instance);
        // 由于这里面实际存储的对象的结构是栈，且每个pattern会先执行ObjectCreateRule，再执行SetPropertiesRule；
        // 则这个函数得到的对象就是ObjectCreateRule类begin方法中实例化的对象。
        // Populate the corresponding properties of the top object
        Object top = digester.peek();
        if (digester.log.isDebugEnabled()) {
            if (top != null) {
                digester.log.debug("[SetPropertiesRule]{" + digester.match +
                                   "} Set " + top.getClass().getName() +
                                   " properties");
            } else {
                digester.log.debug("[SetPropertiesRule]{" + digester.match +
                                   "} Set NULL properties");
            }
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            if ("".equals(name)) {
                name = attributes.getQName(i);
            }
            String value = attributes.getValue(i);

            if (digester.log.isDebugEnabled()) {
                digester.log.debug("[SetPropertiesRule]{" + digester.match +
                        "} Setting property '" + name + "' to '" +
                        value + "'");
            }
            //对属性进行检查；在之前Catalina类的createStartDigester()中设置了Object.class和attrs的对应关系，
            // 如果被检查的属性在attrs中设置过则返回true，不再执行SetPropertiesRule类后面的两个函数。


            //这里最重点的功能在IntrospectionUtils.setProperty(top, name, value)函数；
            // 它的作用是将解析xml所获取到的属性值，设置到之前ObjectCreateRule实例化的对象中，
            // 如果设置失败再进行digester.getRulesValidation()判断，判断是否打印属性值设置失败的日志。
            if (!digester.isFakeAttribute(top, name)
                    && !IntrospectionUtils.setProperty(top, name, value)
                    && digester.getRulesValidation()) {
                digester.log.warn(sm.getString("rule.noProperty", digester.match, name, value));
            }
        }

    }


    /**
     * Render a printable version of this Rule.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SetPropertiesRule[");
        sb.append("]");
        return sb.toString();
    }
}
