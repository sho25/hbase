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
name|hadoop
operator|.
name|io
operator|.
name|Text
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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

begin_comment
comment|/** Tests table creation restrictions*/
end_comment

begin_class
specifier|public
class|class
name|TestTable
extends|extends
name|HBaseClusterTestCase
block|{
comment|/**    * the test    * @throws IOException    */
specifier|public
name|void
name|testCreateTable
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|msg
init|=
literal|null
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|HTableDescriptor
operator|.
name|rootTableDesc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|msg
operator|=
name|e
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Unexcepted exception message "
operator|+
name|msg
argument_list|,
name|msg
operator|!=
literal|null
operator|&&
name|msg
operator|.
name|startsWith
argument_list|(
name|IllegalArgumentException
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|&&
name|msg
operator|.
name|contains
argument_list|(
name|HTableDescriptor
operator|.
name|rootTableDesc
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|msg
operator|=
literal|null
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|HTableDescriptor
operator|.
name|metaTableDesc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|msg
operator|=
name|e
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Unexcepted exception message "
operator|+
name|msg
argument_list|,
name|msg
operator|!=
literal|null
operator|&&
name|msg
operator|.
name|startsWith
argument_list|(
name|IllegalArgumentException
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|&&
name|msg
operator|.
name|contains
argument_list|(
name|HTableDescriptor
operator|.
name|metaTableDesc
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Try doing a duplicate database create.
name|msg
operator|=
literal|null
expr_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableExistsException
name|e
parameter_list|)
block|{
name|msg
operator|=
name|e
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Unexpected exception message "
operator|+
name|msg
argument_list|,
name|msg
operator|!=
literal|null
operator|&&
name|msg
operator|.
name|contains
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now try and do concurrent creation with a bunch of threads.
specifier|final
name|HTableDescriptor
name|threadDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"threaded-"
operator|+
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|threadDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|10
decl_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|count
index|]
decl_stmt|;
specifier|final
name|AtomicInteger
name|successes
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|failures
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|threadDesc
argument_list|)
expr_stmt|;
name|successes
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableExistsException
name|e
parameter_list|)
block|{
name|failures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore.
block|}
block|}
block|}
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
while|while
condition|(
name|threads
index|[
name|i
index|]
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
comment|// All threads are now dead.  Count up how many tables were created and
comment|// how many failed w/ appropriate exception.
name|assertTrue
argument_list|(
name|successes
operator|.
name|get
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|failures
operator|.
name|get
argument_list|()
operator|==
operator|(
name|count
operator|-
literal|1
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test for hadoop-1581 'HBASE: Unopenable tablename bug'.    * @throws Exception    */
specifier|public
name|void
name|testTableNameClash
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|getName
argument_list|()
operator|+
literal|"SOMEUPPERCASE"
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Before fix, below would fail throwing a NoServerForRegionException.
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
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

