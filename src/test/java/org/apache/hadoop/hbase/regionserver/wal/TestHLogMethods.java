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
name|regionserver
operator|.
name|wal
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
name|NavigableSet
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
name|fs
operator|.
name|FSDataOutputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Simple testing of a few HLog methods.  */
end_comment

begin_class
specifier|public
class|class
name|TestHLogMethods
block|{
specifier|private
specifier|final
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Assert that getSplitEditFilesSorted returns files in expected order and    * that it skips moved-aside files.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testGetSplitEditFilesSorted
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|regiondir
init|=
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|(
literal|"regiondir"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|regiondir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|regiondir
argument_list|)
expr_stmt|;
name|Path
name|recoverededits
init|=
name|HLog
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|regiondir
argument_list|)
decl_stmt|;
name|String
name|first
init|=
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|first
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|last
init|=
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|last
argument_list|)
expr_stmt|;
name|createFile
argument_list|(
name|fs
argument_list|,
name|recoverededits
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
operator|+
literal|"."
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|NavigableSet
argument_list|<
name|Path
argument_list|>
name|files
init|=
name|HLog
operator|.
name|getSplitEditFilesSorted
argument_list|(
name|fs
argument_list|,
name|regiondir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|files
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|first
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollLast
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|last
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|files
operator|.
name|pollFirst
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HLog
operator|.
name|formatRecoveredEditsFileName
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createFile
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|testdir
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataOutputStream
name|fdos
init|=
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|testdir
argument_list|,
name|name
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|fdos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

