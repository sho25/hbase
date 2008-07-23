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
name|client
operator|.
name|HTable
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

begin_comment
comment|/** tests administrative functions */
end_comment

begin_class
specifier|public
class|class
name|TestMasterAdmin
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1:"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HTableDescriptor
name|testDesc
decl_stmt|;
static|static
block|{
name|testDesc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"testadmin1"
argument_list|)
expr_stmt|;
name|testDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestMasterAdmin
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|admin
operator|=
literal|null
expr_stmt|;
comment|// Make the thread wake frequency a little slower so other threads
comment|// can run
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.server.thread.wakefrequency"
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
block|}
comment|/** @throws Exception */
specifier|public
name|void
name|testMasterAdmin
parameter_list|()
throws|throws
name|Exception
block|{
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Add test that exception is thrown if descriptor is without a table name.
comment|// HADOOP-2156.
name|boolean
name|exception
init|=
literal|false
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
operator|new
name|HTableDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|exception
operator|=
literal|true
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|exception
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|testDesc
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|testDesc
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" created"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|testDesc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|testDesc
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" disabled"
argument_list|)
expr_stmt|;
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|testDesc
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
block|}
catch|catch
parameter_list|(
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
name|RegionOfflineException
name|e
parameter_list|)
block|{
comment|// Expected
block|}
name|admin
operator|.
name|addColumn
argument_list|(
name|testDesc
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
literal|"col2:"
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|testDesc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|deleteColumn
argument_list|(
name|testDesc
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2:"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotDisabledException
name|e
parameter_list|)
block|{
comment|// Expected
block|}
name|admin
operator|.
name|disableTable
argument_list|(
name|testDesc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteColumn
argument_list|(
name|testDesc
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2:"
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|testDesc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

