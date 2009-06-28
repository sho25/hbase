begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stargate
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HConstants
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
name|client
operator|.
name|HTable
import|;
end_import

begin_class
specifier|public
class|class
name|Test00MiniCluster
extends|extends
name|MiniClusterTestCase
block|{
specifier|public
name|void
name|testDFSMiniCluster
parameter_list|()
block|{
name|assertNotNull
argument_list|(
name|dfsCluster
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testZooKeeperMiniCluster
parameter_list|()
block|{
name|assertNotNull
argument_list|(
name|zooKeeperCluster
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testHBaseMiniCluster
parameter_list|()
throws|throws
name|IOException
block|{
name|assertNotNull
argument_list|(
name|hbaseCluster
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testStargateServlet
parameter_list|()
throws|throws
name|IOException
block|{
name|assertNotNull
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

