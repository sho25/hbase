begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|backup
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|HBaseInterfaceAudience
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
name|backup
operator|.
name|impl
operator|.
name|BackupManager
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
name|backup
operator|.
name|impl
operator|.
name|BackupSystemTable
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
name|Connection
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
name|ConnectionFactory
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
name|RegionInfo
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionObserver
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
name|Pair
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * An Observer to facilitate backup operations  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|BackupObserver
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|BackupObserver
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postBulkLoadHFile
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|stagingFamilyPaths
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Path
argument_list|>
argument_list|>
name|finalPaths
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|cfg
init|=
name|ctx
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
if|if
condition|(
name|finalPaths
operator|==
literal|null
condition|)
block|{
comment|// there is no need to record state
return|return;
block|}
if|if
condition|(
operator|!
name|BackupManager
operator|.
name|isBackupEnabled
argument_list|(
name|cfg
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"skipping recording bulk load in postBulkLoadHFile since backup is disabled"
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|cfg
argument_list|)
init|;
name|BackupSystemTable
name|tbl
operator|=
operator|new
name|BackupSystemTable
argument_list|(
name|connection
argument_list|)
init|)
block|{
name|List
argument_list|<
name|TableName
argument_list|>
name|fullyBackedUpTables
init|=
name|tbl
operator|.
name|getTablesForBackupType
argument_list|(
name|BackupType
operator|.
name|FULL
argument_list|)
decl_stmt|;
name|RegionInfo
name|info
init|=
name|ctx
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|TableName
name|tableName
init|=
name|info
operator|.
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fullyBackedUpTables
operator|.
name|contains
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|tableName
operator|+
literal|" has not gone thru full backup"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|tbl
operator|.
name|writePathsPostBulkLoad
argument_list|(
name|tableName
argument_list|,
name|info
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|finalPaths
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to get tables which have been fully backed up"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCommitStoreFile
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
argument_list|>
name|pairs
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|cfg
init|=
name|ctx
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
if|if
condition|(
name|pairs
operator|==
literal|null
operator|||
name|pairs
operator|.
name|isEmpty
argument_list|()
operator|||
operator|!
name|BackupManager
operator|.
name|isBackupEnabled
argument_list|(
name|cfg
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"skipping recording bulk load in preCommitStoreFile since backup is disabled"
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|cfg
argument_list|)
init|;
name|BackupSystemTable
name|tbl
operator|=
operator|new
name|BackupSystemTable
argument_list|(
name|connection
argument_list|)
init|)
block|{
name|List
argument_list|<
name|TableName
argument_list|>
name|fullyBackedUpTables
init|=
name|tbl
operator|.
name|getTablesForBackupType
argument_list|(
name|BackupType
operator|.
name|FULL
argument_list|)
decl_stmt|;
name|RegionInfo
name|info
init|=
name|ctx
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|TableName
name|tableName
init|=
name|info
operator|.
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fullyBackedUpTables
operator|.
name|contains
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|tableName
operator|+
literal|" has not gone thru full backup"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|tbl
operator|.
name|writeFilesForBulkLoadPreCommit
argument_list|(
name|tableName
argument_list|,
name|info
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|family
argument_list|,
name|pairs
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
end_class

end_unit

