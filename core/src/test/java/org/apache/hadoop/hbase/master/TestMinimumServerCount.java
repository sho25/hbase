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
name|master
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
name|HBaseClusterTestCase
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
name|client
operator|.
name|Result
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
name|ResultScanner
import|;
end_import

begin_class
specifier|public
class|class
name|TestMinimumServerCount
extends|extends
name|HBaseClusterTestCase
block|{
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"TestTable"
decl_stmt|;
specifier|public
name|TestMinimumServerCount
parameter_list|()
block|{
comment|// start cluster with one region server only
name|super
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isTableAvailable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|available
init|=
literal|true
decl_stmt|;
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
literal|".META."
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|meta
operator|.
name|getScanner
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|Result
name|result
decl_stmt|;
while|while
condition|(
operator|(
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
comment|// set available to false if a region of the table is found with no
comment|// assigned server
name|byte
index|[]
name|value
init|=
name|result
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|available
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
return|return
name|available
return|;
block|}
specifier|public
name|void
name|testMinimumServerCount
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
comment|// create and disable table
name|admin
operator|.
name|createTable
argument_list|(
name|createTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// reach in and set minimum server count
name|cluster
operator|.
name|hbaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|setMinimumServerCount
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|// now try to enable the table
try|try
block|{
name|admin
operator|.
name|enableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// ignore
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// now start another region server
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
comment|// sleep a bit for assignment
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

