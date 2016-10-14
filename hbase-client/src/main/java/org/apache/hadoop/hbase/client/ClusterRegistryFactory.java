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
operator|.
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|classification
operator|.
name|InterfaceAudience
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

begin_comment
comment|/**  * Get instance of configured Registry.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
class|class
name|ClusterRegistryFactory
block|{
specifier|static
specifier|final
name|String
name|REGISTRY_IMPL_CONF_KEY
init|=
literal|"hbase.client.registry.impl"
decl_stmt|;
specifier|private
name|ClusterRegistryFactory
parameter_list|()
block|{   }
comment|/**    * @return The cluster registry implementation to use.    */
specifier|static
name|ClusterRegistry
name|getRegistry
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|ClusterRegistry
argument_list|>
name|clazz
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|REGISTRY_IMPL_CONF_KEY
argument_list|,
name|ZKClusterRegistry
operator|.
name|class
argument_list|,
name|ClusterRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

