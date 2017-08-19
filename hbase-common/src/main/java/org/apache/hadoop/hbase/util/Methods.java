begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|UndeclaredThrowableException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Methods
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Methods
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Object
name|call
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|,
name|T
name|instance
parameter_list|,
name|String
name|methodName
parameter_list|,
name|Class
index|[]
name|types
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|Method
name|m
init|=
name|clazz
operator|.
name|getMethod
argument_list|(
name|methodName
argument_list|,
name|types
argument_list|)
decl_stmt|;
return|return
name|m
operator|.
name|invoke
argument_list|(
name|instance
argument_list|,
name|args
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|arge
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Constructed invalid call. class="
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|" method="
operator|+
name|methodName
operator|+
literal|" types="
operator|+
name|Classes
operator|.
name|stringify
argument_list|(
name|types
argument_list|)
argument_list|,
name|arge
argument_list|)
expr_stmt|;
throw|throw
name|arge
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|nsme
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't find method "
operator|+
name|methodName
operator|+
literal|" in "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|"!"
argument_list|,
name|nsme
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
comment|// unwrap the underlying exception and rethrow
if|if
condition|(
name|ite
operator|.
name|getTargetException
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ite
operator|.
name|getTargetException
argument_list|()
operator|instanceof
name|Exception
condition|)
block|{
throw|throw
operator|(
name|Exception
operator|)
name|ite
operator|.
name|getTargetException
argument_list|()
throw|;
block|}
elseif|else
if|if
condition|(
name|ite
operator|.
name|getTargetException
argument_list|()
operator|instanceof
name|Error
condition|)
block|{
throw|throw
operator|(
name|Error
operator|)
name|ite
operator|.
name|getTargetException
argument_list|()
throw|;
block|}
block|}
throw|throw
operator|new
name|UndeclaredThrowableException
argument_list|(
name|ite
argument_list|,
literal|"Unknown exception invoking "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|methodName
operator|+
literal|"()"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|iae
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Denied access calling "
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|methodName
operator|+
literal|"()"
argument_list|,
name|iae
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|se
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"SecurityException calling method. class="
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|" method="
operator|+
name|methodName
operator|+
literal|" types="
operator|+
name|Classes
operator|.
name|stringify
argument_list|(
name|types
argument_list|)
argument_list|,
name|se
argument_list|)
expr_stmt|;
throw|throw
name|se
throw|;
block|}
block|}
block|}
end_class

end_unit

