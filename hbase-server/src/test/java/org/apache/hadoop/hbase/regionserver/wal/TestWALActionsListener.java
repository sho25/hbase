begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|*
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
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
name|*
import|;
end_import

begin_comment
comment|/**  * Test that the actions are called while playing with an HLog  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestWALActionsListener
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestWALActionsListener
operator|.
name|class
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
name|byte
index|[]
name|SOME_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"t"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Path
name|oldLogDir
decl_stmt|;
specifier|private
specifier|static
name|Path
name|logDir
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
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
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogs"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|oldLogDir
operator|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
expr_stmt|;
name|logDir
operator|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|fs
operator|.
name|delete
argument_list|(
name|logDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|oldLogDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|setUp
argument_list|()
expr_stmt|;
block|}
comment|/**    * Add a bunch of dummy data and roll the logs every two insert. We    * should end up with 10 rolled files (plus the roll called in    * the constructor). Also test adding a listener while it's running.    */
annotation|@
name|Test
specifier|public
name|void
name|testActionListener
parameter_list|()
throws|throws
name|Exception
block|{
name|DummyWALActionsListener
name|observer
init|=
operator|new
name|DummyWALActionsListener
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|WALActionsListener
argument_list|>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|observer
argument_list|)
expr_stmt|;
name|DummyWALActionsListener
name|laterobserver
init|=
operator|new
name|DummyWALActionsListener
argument_list|()
decl_stmt|;
name|HLog
name|hlog
init|=
operator|new
name|HLog
argument_list|(
name|fs
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|,
name|conf
argument_list|,
name|list
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|SOME_BYTES
argument_list|,
name|SOME_BYTES
argument_list|,
name|SOME_BYTES
argument_list|,
literal|false
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
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
literal|""
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|b
argument_list|,
name|b
argument_list|)
decl_stmt|;
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|edit
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|()
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|b
argument_list|,
name|b
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
argument_list|)
decl_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|key
argument_list|,
name|edit
argument_list|,
name|htd
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
literal|10
condition|)
block|{
name|hlog
operator|.
name|registerWALActionsListener
argument_list|(
name|laterobserver
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
name|hlog
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
block|}
block|}
name|hlog
operator|.
name|close
argument_list|()
expr_stmt|;
name|hlog
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|11
argument_list|,
name|observer
operator|.
name|preLogRollCounter
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|11
argument_list|,
name|observer
operator|.
name|postLogRollCounter
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|laterobserver
operator|.
name|preLogRollCounter
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|laterobserver
operator|.
name|postLogRollCounter
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|observer
operator|.
name|closedCount
argument_list|)
expr_stmt|;
block|}
comment|/**    * Just counts when methods are called    */
specifier|static
class|class
name|DummyWALActionsListener
implements|implements
name|WALActionsListener
block|{
specifier|public
name|int
name|preLogRollCounter
init|=
literal|0
decl_stmt|;
specifier|public
name|int
name|postLogRollCounter
init|=
literal|0
decl_stmt|;
specifier|public
name|int
name|closedCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|preLogRoll
parameter_list|(
name|Path
name|oldFile
parameter_list|,
name|Path
name|newFile
parameter_list|)
block|{
name|preLogRollCounter
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postLogRoll
parameter_list|(
name|Path
name|oldFile
parameter_list|,
name|Path
name|newFile
parameter_list|)
block|{
name|postLogRollCounter
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preLogArchive
parameter_list|(
name|Path
name|oldFile
parameter_list|,
name|Path
name|newFile
parameter_list|)
block|{
comment|// Not interested
block|}
annotation|@
name|Override
specifier|public
name|void
name|postLogArchive
parameter_list|(
name|Path
name|oldFile
parameter_list|,
name|Path
name|newFile
parameter_list|)
block|{
comment|// Not interested
block|}
annotation|@
name|Override
specifier|public
name|void
name|logRollRequested
parameter_list|()
block|{
comment|// Not interested
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
block|{
comment|// Not interested
block|}
annotation|@
name|Override
specifier|public
name|void
name|logCloseRequested
parameter_list|()
block|{
name|closedCount
operator|++
expr_stmt|;
block|}
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
block|{
comment|//To change body of implemented methods use File | Settings | File Templates.
block|}
block|}
block|}
end_class

end_unit

