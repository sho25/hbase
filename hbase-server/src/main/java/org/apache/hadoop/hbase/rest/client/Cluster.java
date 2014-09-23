begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
operator|.
name|client
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
name|Collections
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * A list of 'host:port' addresses of HTTP servers operating as a single  * entity, for example multiple redundant web service gateways.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|Cluster
block|{
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|Collections
operator|.
name|synchronizedList
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|String
name|lastHost
decl_stmt|;
comment|/**    * Constructor    */
specifier|public
name|Cluster
parameter_list|()
block|{}
comment|/**    * Constructor    * @param nodes a list of service locations, in 'host:port' format    */
specifier|public
name|Cluster
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|nodes
parameter_list|)
block|{
name|nodes
operator|.
name|addAll
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return true if no locations have been added, false otherwise    */
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|nodes
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * Add a node to the cluster    * @param node the service location in 'host:port' format    */
specifier|public
name|Cluster
name|add
parameter_list|(
name|String
name|node
parameter_list|)
block|{
name|nodes
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Add a node to the cluster    * @param name host name    * @param port service port    */
specifier|public
name|Cluster
name|add
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|port
argument_list|)
expr_stmt|;
return|return
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Remove a node from the cluster    * @param node the service location in 'host:port' format    */
specifier|public
name|Cluster
name|remove
parameter_list|(
name|String
name|node
parameter_list|)
block|{
name|nodes
operator|.
name|remove
argument_list|(
name|node
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Remove a node from the cluster    * @param name host name    * @param port service port    */
specifier|public
name|Cluster
name|remove
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|port
argument_list|)
expr_stmt|;
return|return
name|remove
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

