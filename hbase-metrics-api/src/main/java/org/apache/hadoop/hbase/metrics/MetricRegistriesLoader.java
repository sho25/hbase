begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metrics
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ReflectionUtils
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|MetricRegistriesLoader
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MetricRegistries
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|defaultClass
init|=
literal|"org.apache.hadoop.hbase.metrics.impl.MetricRegistriesImpl"
decl_stmt|;
specifier|private
name|MetricRegistriesLoader
parameter_list|()
block|{   }
comment|/**    * Creates a {@link MetricRegistries} instance using the corresponding {@link MetricRegistries}    * available to {@link ServiceLoader} on the classpath. If no instance is found, then default    * implementation will be loaded.    * @return A {@link MetricRegistries} implementation.    */
specifier|public
specifier|static
name|MetricRegistries
name|load
parameter_list|()
block|{
name|List
argument_list|<
name|MetricRegistries
argument_list|>
name|availableImplementations
init|=
name|getDefinedImplemantations
argument_list|()
decl_stmt|;
return|return
name|load
argument_list|(
name|availableImplementations
argument_list|)
return|;
block|}
comment|/**    * Creates a {@link MetricRegistries} instance using the corresponding {@link MetricRegistries}    * available to {@link ServiceLoader} on the classpath. If no instance is found, then default    * implementation will be loaded.    * @return A {@link MetricRegistries} implementation.    */
annotation|@
name|VisibleForTesting
specifier|static
name|MetricRegistries
name|load
parameter_list|(
name|List
argument_list|<
name|MetricRegistries
argument_list|>
name|availableImplementations
parameter_list|)
block|{
if|if
condition|(
name|availableImplementations
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// One and only one instance -- what we want/expect
name|MetricRegistries
name|impl
init|=
name|availableImplementations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Loaded MetricRegistries "
operator|+
name|impl
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|impl
return|;
block|}
elseif|else
if|if
condition|(
name|availableImplementations
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
try|try
block|{
return|return
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
operator|(
name|Class
argument_list|<
name|MetricRegistries
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|defaultClass
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// Tell the user they're doing something wrong, and choose the first impl.
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|MetricRegistries
name|factory
range|:
name|availableImplementations
control|)
block|{
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|factory
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found multiple MetricRegistries implementations: "
operator|+
name|sb
operator|+
literal|". Using first found implementation: "
operator|+
name|availableImplementations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|availableImplementations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|MetricRegistries
argument_list|>
name|getDefinedImplemantations
parameter_list|()
block|{
name|ServiceLoader
argument_list|<
name|MetricRegistries
argument_list|>
name|loader
init|=
name|ServiceLoader
operator|.
name|load
argument_list|(
name|MetricRegistries
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|MetricRegistries
argument_list|>
name|availableFactories
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|MetricRegistries
name|impl
range|:
name|loader
control|)
block|{
name|availableFactories
operator|.
name|add
argument_list|(
name|impl
argument_list|)
expr_stmt|;
block|}
return|return
name|availableFactories
return|;
block|}
block|}
end_class

end_unit

