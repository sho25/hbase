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
name|master
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
name|Arrays
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
name|hadoop
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
name|ServerName
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
name|hadoop
operator|.
name|net
operator|.
name|DNSToSwitchMapping
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
name|net
operator|.
name|ScriptBasedMapping
import|;
end_import

begin_comment
comment|/**  * Wrapper over the rack resolution utility in Hadoop. The rack resolution  * utility in Hadoop does resolution from hosts to the racks they belong to.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RackManager
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RackManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNKNOWN_RACK
init|=
literal|"Unknown Rack"
decl_stmt|;
specifier|private
name|DNSToSwitchMapping
name|switchMapping
decl_stmt|;
specifier|public
name|RackManager
parameter_list|()
block|{   }
specifier|public
name|RackManager
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|switchMapping
operator|=
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|conf
operator|.
name|getClass
argument_list|(
literal|"hbase.util.ip.to.rack.determiner"
argument_list|,
name|ScriptBasedMapping
operator|.
name|class
argument_list|,
name|DNSToSwitchMapping
operator|.
name|class
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|Configuration
operator|.
name|class
block|}
operator|,
operator|new
name|Object
index|[]
block|{
name|conf
block|}
block|)
empty_stmt|;
block|}
end_class

begin_comment
comment|/**    * Get the name of the rack containing a server, according to the DNS to    * switch mapping.    * @param server the server for which to get the rack name    * @return the rack name of the server    */
end_comment

begin_function
specifier|public
name|String
name|getRack
parameter_list|(
name|ServerName
name|server
parameter_list|)
block|{
if|if
condition|(
name|server
operator|==
literal|null
condition|)
block|{
return|return
name|UNKNOWN_RACK
return|;
block|}
comment|// just a note - switchMapping caches results (at least the implementation should unless the
comment|// resolution is really a lightweight process)
name|List
argument_list|<
name|String
argument_list|>
name|racks
init|=
name|switchMapping
operator|.
name|resolve
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|server
operator|.
name|getHostname
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|racks
operator|!=
literal|null
operator|&&
operator|!
name|racks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|racks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
return|return
name|UNKNOWN_RACK
return|;
block|}
end_function

begin_comment
comment|/**    * Same as {@link #getRack(ServerName)} except that a list is passed    * @param servers    * @return list of racks for the given list of servers    */
end_comment

begin_function
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getRack
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
block|{
comment|// just a note - switchMapping caches results (at least the implementation should unless the
comment|// resolution is really a lightweight process)
name|List
argument_list|<
name|String
argument_list|>
name|serversAsString
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|servers
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ServerName
name|server
range|:
name|servers
control|)
block|{
name|serversAsString
operator|.
name|add
argument_list|(
name|server
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|racks
init|=
name|switchMapping
operator|.
name|resolve
argument_list|(
name|serversAsString
argument_list|)
decl_stmt|;
return|return
name|racks
return|;
block|}
end_function

unit|}
end_unit

