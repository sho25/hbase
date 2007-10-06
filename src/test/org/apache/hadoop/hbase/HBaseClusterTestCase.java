begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Abstract base class for HBase cluster junit tests.  Spins up cluster on  * {@link #setUp()} and takes it down again in {@link #tearDown()}.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HBaseClusterTestCase
extends|extends
name|HBaseTestCase
block|{
specifier|protected
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|final
name|boolean
name|miniHdfs
decl_stmt|;
name|int
name|regionServers
decl_stmt|;
comment|/**    * constructor    */
specifier|public
name|HBaseClusterTestCase
parameter_list|()
block|{
name|this
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param regionServers    */
specifier|public
name|HBaseClusterTestCase
parameter_list|(
name|int
name|regionServers
parameter_list|)
block|{
name|this
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionServers
operator|=
name|regionServers
expr_stmt|;
block|}
comment|/**    * @param name    */
specifier|public
name|HBaseClusterTestCase
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param miniHdfs    */
specifier|public
name|HBaseClusterTestCase
parameter_list|(
specifier|final
name|boolean
name|miniHdfs
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|miniHdfs
operator|=
name|miniHdfs
expr_stmt|;
name|this
operator|.
name|regionServers
operator|=
literal|1
expr_stmt|;
block|}
comment|/**    * @param name    * @param miniHdfs    */
specifier|public
name|HBaseClusterTestCase
parameter_list|(
name|String
name|name
parameter_list|,
specifier|final
name|boolean
name|miniHdfs
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|miniHdfs
operator|=
name|miniHdfs
expr_stmt|;
name|this
operator|.
name|regionServers
operator|=
literal|1
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|cluster
operator|=
operator|new
name|MiniHBaseCluster
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|regionServers
argument_list|,
name|this
operator|.
name|miniHdfs
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|cluster
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|HConnectionManager
operator|.
name|deleteConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

