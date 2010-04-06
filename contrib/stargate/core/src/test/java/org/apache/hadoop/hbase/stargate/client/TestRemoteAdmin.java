begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|HBaseAdmin
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
name|stargate
operator|.
name|MiniClusterTestBase
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
name|stargate
operator|.
name|client
operator|.
name|Client
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
name|Bytes
import|;
end_import

begin_class
specifier|public
class|class
name|TestRemoteAdmin
extends|extends
name|MiniClusterTestBase
block|{
specifier|static
specifier|final
name|String
name|TABLE_1
init|=
literal|"TestRemoteAdmin_Table_1"
decl_stmt|;
specifier|static
specifier|final
name|String
name|TABLE_2
init|=
literal|"TestRemoteAdmin_Table_2"
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|HTableDescriptor
name|DESC_1
decl_stmt|;
static|static
block|{
name|DESC_1
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_1
argument_list|)
expr_stmt|;
name|DESC_1
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|final
name|HTableDescriptor
name|DESC_2
decl_stmt|;
static|static
block|{
name|DESC_2
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_2
argument_list|)
expr_stmt|;
name|DESC_2
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Client
name|client
decl_stmt|;
name|HBaseAdmin
name|localAdmin
decl_stmt|;
name|RemoteAdmin
name|remoteAdmin
decl_stmt|;
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
name|localAdmin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|remoteAdmin
operator|=
operator|new
name|RemoteAdmin
argument_list|(
operator|new
name|Client
argument_list|(
operator|new
name|Cluster
argument_list|()
operator|.
name|add
argument_list|(
literal|"localhost"
argument_list|,
name|testServletPort
argument_list|)
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|localAdmin
operator|.
name|tableExists
argument_list|(
name|TABLE_1
argument_list|)
condition|)
block|{
name|localAdmin
operator|.
name|disableTable
argument_list|(
name|TABLE_1
argument_list|)
expr_stmt|;
name|localAdmin
operator|.
name|deleteTable
argument_list|(
name|TABLE_1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|localAdmin
operator|.
name|tableExists
argument_list|(
name|TABLE_2
argument_list|)
condition|)
block|{
name|localAdmin
operator|.
name|createTable
argument_list|(
name|DESC_2
argument_list|)
expr_stmt|;
block|}
block|}
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
block|}
specifier|public
name|void
name|testCreateTable
parameter_list|()
throws|throws
name|Exception
block|{
name|assertFalse
argument_list|(
name|remoteAdmin
operator|.
name|isTableAvailable
argument_list|(
name|TABLE_1
argument_list|)
argument_list|)
expr_stmt|;
name|remoteAdmin
operator|.
name|createTable
argument_list|(
name|DESC_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|remoteAdmin
operator|.
name|isTableAvailable
argument_list|(
name|TABLE_1
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testDeleteTable
parameter_list|()
throws|throws
name|Exception
block|{
name|assertTrue
argument_list|(
name|remoteAdmin
operator|.
name|isTableAvailable
argument_list|(
name|TABLE_2
argument_list|)
argument_list|)
expr_stmt|;
name|remoteAdmin
operator|.
name|deleteTable
argument_list|(
name|TABLE_2
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|remoteAdmin
operator|.
name|isTableAvailable
argument_list|(
name|TABLE_2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

