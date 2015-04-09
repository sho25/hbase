begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|procedure
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
name|HRegionInfo
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
name|HRegionLocation
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
name|MetaTableAccessor
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
name|RegionLocations
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
name|ServerName
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
name|TableName
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
name|TableDescriptor
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
name|master
operator|.
name|HMaster
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
name|procedure2
operator|.
name|ProcedureExecutor
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
name|procedure2
operator|.
name|ProcedureTestingUtility
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
name|ModifyRegionUtils
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|FSUtils
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|assertTrue
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
name|fail
import|;
end_import

begin_class
specifier|public
class|class
name|MasterProcedureTestingUtility
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MasterProcedureTestingUtility
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MasterProcedureTestingUtility
parameter_list|()
block|{   }
specifier|public
specifier|static
name|HTableDescriptor
name|createHTD
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
modifier|...
name|family
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
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
name|family
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|htd
return|;
block|}
specifier|public
specifier|static
name|HRegionInfo
index|[]
name|createTable
parameter_list|(
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|,
name|String
modifier|...
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
name|createHTD
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|HRegionInfo
index|[]
name|regions
init|=
name|ModifyRegionUtils
operator|.
name|createHRegionInfos
argument_list|(
name|htd
argument_list|,
name|splitKeys
argument_list|)
decl_stmt|;
name|long
name|procId
init|=
name|ProcedureTestingUtility
operator|.
name|submitAndWait
argument_list|(
name|procExec
argument_list|,
operator|new
name|CreateTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
name|regions
argument_list|)
argument_list|)
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|regions
return|;
block|}
specifier|public
specifier|static
name|void
name|validateTableCreation
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|HRegionInfo
index|[]
name|regions
parameter_list|,
name|String
modifier|...
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|validateTableCreation
argument_list|(
name|master
argument_list|,
name|tableName
argument_list|,
name|regions
argument_list|,
literal|true
argument_list|,
name|family
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|validateTableCreation
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|HRegionInfo
index|[]
name|regions
parameter_list|,
name|boolean
name|hasFamilyDirs
parameter_list|,
name|String
modifier|...
name|family
parameter_list|)
throws|throws
name|IOException
block|{
comment|// check filesystem
specifier|final
name|FileSystem
name|fs
init|=
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|tableDir
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|allRegionDirs
init|=
name|FSUtils
operator|.
name|getRegionDirs
argument_list|(
name|fs
argument_list|,
name|tableDir
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
name|regions
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|regions
index|[
name|i
index|]
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|regions
index|[
name|i
index|]
operator|+
literal|" region dir does not exist"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|regionDir
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|allRegionDirs
operator|.
name|remove
argument_list|(
name|regionDir
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|allFamilyDirs
init|=
name|FSUtils
operator|.
name|getFamilyDirs
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|family
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|family
index|[
name|j
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasFamilyDirs
condition|)
block|{
name|assertTrue
argument_list|(
name|family
index|[
name|j
index|]
operator|+
literal|" family dir does not exist"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|familyDir
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|allFamilyDirs
operator|.
name|remove
argument_list|(
name|familyDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: WARN: Modify Table/Families does not create a family dir
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|familyDir
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|family
index|[
name|j
index|]
operator|+
literal|" family dir does not exist"
argument_list|)
expr_stmt|;
block|}
name|allFamilyDirs
operator|.
name|remove
argument_list|(
name|familyDir
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
literal|"found extraneous families: "
operator|+
name|allFamilyDirs
argument_list|,
name|allFamilyDirs
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"found extraneous regions: "
operator|+
name|allRegionDirs
argument_list|,
name|allRegionDirs
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// check meta
name|assertTrue
argument_list|(
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|regions
operator|.
name|length
argument_list|,
name|countMetaRegions
argument_list|(
name|master
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// check htd
name|TableDescriptor
name|tableDesc
init|=
name|master
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|getDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"table descriptor not found"
argument_list|,
name|tableDesc
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|tableDesc
operator|.
name|getHTableDescriptor
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"table descriptor not found"
argument_list|,
name|htd
operator|!=
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|family
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
literal|"family not found "
operator|+
name|family
index|[
name|i
index|]
argument_list|,
name|htd
operator|.
name|getFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
index|[
name|i
index|]
argument_list|)
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|family
operator|.
name|length
argument_list|,
name|htd
operator|.
name|getFamilies
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|validateTableDeletion
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|HRegionInfo
index|[]
name|regions
parameter_list|,
name|String
modifier|...
name|family
parameter_list|)
throws|throws
name|IOException
block|{
comment|// check filesystem
specifier|final
name|FileSystem
name|fs
init|=
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|tableDir
argument_list|)
argument_list|)
expr_stmt|;
comment|// check meta
name|assertFalse
argument_list|(
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countMetaRegions
argument_list|(
name|master
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// check htd
name|assertTrue
argument_list|(
literal|"found htd of deleted table"
argument_list|,
name|master
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|getDescriptor
argument_list|(
name|tableName
argument_list|)
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|int
name|countMetaRegions
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|AtomicInteger
name|actualRegCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|MetaTableAccessor
operator|.
name|Visitor
name|visitor
init|=
operator|new
name|MetaTableAccessor
operator|.
name|Visitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
name|Result
name|rowResult
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionLocations
name|list
init|=
name|MetaTableAccessor
operator|.
name|getRegionLocations
argument_list|(
name|rowResult
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No serialized HRegionInfo in "
operator|+
name|rowResult
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|HRegionLocation
name|l
init|=
name|list
operator|.
name|getRegionLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|l
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
name|l
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|l
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isOffline
argument_list|()
operator|||
name|l
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isSplit
argument_list|()
condition|)
return|return
literal|true
return|;
name|HRegionLocation
index|[]
name|locations
init|=
name|list
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|location
range|:
name|locations
control|)
block|{
if|if
condition|(
name|location
operator|==
literal|null
condition|)
continue|continue;
name|ServerName
name|serverName
init|=
name|location
operator|.
name|getServerName
argument_list|()
decl_stmt|;
comment|// Make sure that regions are assigned to server
if|if
condition|(
name|serverName
operator|!=
literal|null
operator|&&
name|serverName
operator|.
name|getHostAndPort
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|actualRegCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
name|MetaTableAccessor
operator|.
name|scanMetaForTableRegions
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|visitor
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
return|return
name|actualRegCount
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TState
parameter_list|>
name|void
name|testRecoveryAndDoubleExecution
parameter_list|(
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
parameter_list|,
specifier|final
name|long
name|procId
parameter_list|,
specifier|final
name|int
name|numSteps
parameter_list|,
specifier|final
name|TState
index|[]
name|states
parameter_list|)
throws|throws
name|Exception
block|{
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|procExec
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
comment|// Restart the executor and execute the step twice
comment|//   execute step N - kill before store update
comment|//   restart executor/store
comment|//   execute step N - save on store
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numSteps
condition|;
operator|++
name|i
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart "
operator|+
name|i
operator|+
literal|" exec state: "
operator|+
name|states
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|procExec
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TState
parameter_list|>
name|void
name|testRollbackAndDoubleExecution
parameter_list|(
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
parameter_list|,
specifier|final
name|long
name|procId
parameter_list|,
specifier|final
name|int
name|lastStep
parameter_list|,
specifier|final
name|TState
index|[]
name|states
parameter_list|)
throws|throws
name|Exception
block|{
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
comment|// Restart the executor and execute the step twice
comment|//   execute step N - kill before store update
comment|//   restart executor/store
comment|//   execute step N - save on store
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|lastStep
condition|;
operator|++
name|i
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart "
operator|+
name|i
operator|+
literal|" exec state: "
operator|+
name|states
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
comment|// Restart the executor and rollback the step twice
comment|//   rollback step N - kill before store update
comment|//   restart executor/store
comment|//   rollback step N - save on store
name|MasterProcedureTestingUtility
operator|.
name|InjectAbortOnLoadListener
name|abortListener
init|=
operator|new
name|MasterProcedureTestingUtility
operator|.
name|InjectAbortOnLoadListener
argument_list|(
name|procExec
argument_list|)
decl_stmt|;
name|procExec
operator|.
name|registerListener
argument_list|(
name|abortListener
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
name|lastStep
operator|+
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart "
operator|+
name|i
operator|+
literal|" rollback state: "
operator|+
name|states
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|procExec
operator|.
name|unregisterListener
argument_list|(
name|abortListener
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ProcedureTestingUtility
operator|.
name|assertIsAbortException
argument_list|(
name|procExec
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TState
parameter_list|>
name|void
name|testRollbackAndDoubleExecutionAfterPONR
parameter_list|(
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
parameter_list|,
specifier|final
name|long
name|procId
parameter_list|,
specifier|final
name|int
name|lastStep
parameter_list|,
specifier|final
name|TState
index|[]
name|states
parameter_list|)
throws|throws
name|Exception
block|{
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
comment|// Restart the executor and execute the step twice
comment|//   execute step N - kill before store update
comment|//   restart executor/store
comment|//   execute step N - save on store
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|lastStep
condition|;
operator|++
name|i
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart "
operator|+
name|i
operator|+
literal|" exec state: "
operator|+
name|states
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
comment|// try to inject the abort
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|InjectAbortOnLoadListener
name|abortListener
init|=
operator|new
name|MasterProcedureTestingUtility
operator|.
name|InjectAbortOnLoadListener
argument_list|(
name|procExec
argument_list|)
decl_stmt|;
name|procExec
operator|.
name|registerListener
argument_list|(
name|abortListener
argument_list|)
expr_stmt|;
try|try
block|{
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart and execute"
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|procExec
operator|.
name|unregisterListener
argument_list|(
name|abortListener
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|procExec
operator|.
name|isRunning
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotFailed
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|TState
parameter_list|>
name|void
name|testRollbackRetriableFailure
parameter_list|(
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
parameter_list|,
specifier|final
name|long
name|procId
parameter_list|,
specifier|final
name|int
name|lastStep
parameter_list|,
specifier|final
name|TState
index|[]
name|states
parameter_list|)
throws|throws
name|Exception
block|{
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
comment|// Restart the executor and execute the step twice
comment|//   execute step N - kill before store update
comment|//   restart executor/store
comment|//   execute step N - save on store
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|lastStep
condition|;
operator|++
name|i
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart "
operator|+
name|i
operator|+
literal|" exec state: "
operator|+
name|states
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
comment|// execute the rollback
name|ProcedureTestingUtility
operator|.
name|setKillAndToggleBeforeStoreUpdate
argument_list|(
name|procExec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MasterProcedureTestingUtility
operator|.
name|InjectAbortOnLoadListener
name|abortListener
init|=
operator|new
name|MasterProcedureTestingUtility
operator|.
name|InjectAbortOnLoadListener
argument_list|(
name|procExec
argument_list|)
decl_stmt|;
name|procExec
operator|.
name|registerListener
argument_list|(
name|abortListener
argument_list|)
expr_stmt|;
try|try
block|{
name|ProcedureTestingUtility
operator|.
name|assertProcNotYetCompleted
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|restart
argument_list|(
name|procExec
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restart and rollback"
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|procExec
operator|.
name|unregisterListener
argument_list|(
name|abortListener
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ProcedureTestingUtility
operator|.
name|assertIsAbortException
argument_list|(
name|procExec
operator|.
name|getResult
argument_list|(
name|procId
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|InjectAbortOnLoadListener
implements|implements
name|ProcedureExecutor
operator|.
name|ProcedureExecutorListener
block|{
specifier|private
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
decl_stmt|;
specifier|public
name|InjectAbortOnLoadListener
parameter_list|(
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
parameter_list|)
block|{
name|this
operator|.
name|procExec
operator|=
name|procExec
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|procedureLoaded
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
name|procExec
operator|.
name|abort
argument_list|(
name|procId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|procedureAdded
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
comment|/* no-op */
block|}
annotation|@
name|Override
specifier|public
name|void
name|procedureFinished
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
comment|/* no-op */
block|}
block|}
block|}
end_class

end_unit

