begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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
name|HBaseTestingUtility
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
name|HServerInfo
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
name|Put
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
name|Scan
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_class
specifier|public
class|class
name|TestHBaseFsck
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHBaseFsck
parameter_list|()
throws|throws
name|IOException
block|{
name|HBaseFsck
name|fsck
init|=
operator|new
name|HBaseFsck
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fsck
operator|.
name|displayFullReport
argument_list|()
expr_stmt|;
name|fsck
operator|.
name|setTimeLag
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Most basic check ever, 0 tables
name|int
name|result
init|=
name|fsck
operator|.
name|doWork
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAM
argument_list|)
expr_stmt|;
comment|// We created 1 table, should be fine
name|result
operator|=
name|fsck
operator|.
name|doWork
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// Now let's mess it up and change the assignment in .META. to
comment|// point to a different region server
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HTableDescriptor
operator|.
name|META_TABLEDESC
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|meta
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|resforloop
label|:
for|for
control|(
name|Result
name|res
range|:
name|scanner
control|)
block|{
name|long
name|startCode
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|STARTCODE_QUALIFIER
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rs
range|:
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|HServerInfo
name|hsi
init|=
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerInfo
argument_list|()
decl_stmt|;
comment|// When we find a diff RS, change the assignment and break
if|if
condition|(
name|startCode
operator|!=
name|hsi
operator|.
name|getStartCode
argument_list|()
condition|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hsi
operator|.
name|getHostnamePort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|STARTCODE_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hsi
operator|.
name|getStartCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|meta
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
break|break
name|resforloop
break|;
block|}
block|}
block|}
comment|// We set this here, but it's really not fixing anything...
name|fsck
operator|.
name|setFixErrors
argument_list|()
expr_stmt|;
name|result
operator|=
name|fsck
operator|.
name|doWork
argument_list|()
expr_stmt|;
comment|// Fixed or not, it still reports inconsistencies
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// Disabled, won't work because the region stays unassigned, see HBASE-3217
comment|// new HTable(conf, TABLE).getScanner(new Scan());
block|}
block|}
end_class

end_unit

