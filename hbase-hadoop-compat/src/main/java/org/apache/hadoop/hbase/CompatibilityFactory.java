begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ServiceLoader
import|;
end_import

begin_comment
comment|/**  * Class that will create many instances of classes provided by the hbase-hadoop{1|2}-compat jars.  */
end_comment

begin_class
specifier|public
class|class
name|CompatibilityFactory
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
name|CompatibilitySingletonFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXCEPTION_START
init|=
literal|"Could not create  "
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXCEPTION_END
init|=
literal|" Is the hadoop compatibility jar on the classpath?"
decl_stmt|;
comment|/**    * This is a static only class don't let any instance be created.    */
specifier|protected
name|CompatibilityFactory
parameter_list|()
block|{}
specifier|public
specifier|static
specifier|synchronized
parameter_list|<
name|T
parameter_list|>
name|T
name|getInstance
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|klass
parameter_list|)
block|{
name|T
name|instance
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ServiceLoader
argument_list|<
name|T
argument_list|>
name|loader
init|=
name|ServiceLoader
operator|.
name|load
argument_list|(
name|klass
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|T
argument_list|>
name|it
init|=
name|loader
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|instance
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|StringBuilder
name|msg
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"ServiceLoader provided more than one implementation for class: "
argument_list|)
operator|.
name|append
argument_list|(
name|klass
argument_list|)
operator|.
name|append
argument_list|(
literal|", using implementation: "
argument_list|)
operator|.
name|append
argument_list|(
name|instance
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", other implementations: {"
argument_list|)
expr_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|msg
operator|.
name|append
argument_list|(
name|it
operator|.
name|next
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|msg
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|createExceptionString
argument_list|(
name|klass
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Error
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|createExceptionString
argument_list|(
name|klass
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// If there was nothing returned and no exception then throw an exception.
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|createExceptionString
argument_list|(
name|klass
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|instance
return|;
block|}
specifier|protected
specifier|static
name|String
name|createExceptionString
parameter_list|(
name|Class
name|klass
parameter_list|)
block|{
return|return
name|EXCEPTION_START
operator|+
name|klass
operator|.
name|toString
argument_list|()
operator|+
name|EXCEPTION_END
return|;
block|}
block|}
end_class

end_unit

