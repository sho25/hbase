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
name|util
operator|.
name|hbck
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
name|assertEquals
import|;
end_import

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
name|Arrays
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
name|concurrent
operator|.
name|ExecutorService
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
name|ScheduledThreadPoolExecutor
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
name|util
operator|.
name|HBaseFsck
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
name|HBaseFsck
operator|.
name|ErrorReporter
operator|.
name|ERROR_CODE
import|;
end_import

begin_class
specifier|public
class|class
name|HbckTestingUtil
block|{
specifier|private
specifier|static
name|ExecutorService
name|exec
init|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|10
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|HBaseFsck
name|doFsck
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|fix
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|doFsck
argument_list|(
name|conf
argument_list|,
name|fix
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HBaseFsck
name|doFsck
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|fix
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|doFsck
argument_list|(
name|conf
argument_list|,
name|fix
argument_list|,
name|fix
argument_list|,
name|fix
argument_list|,
name|fix
argument_list|,
name|fix
argument_list|,
name|fix
argument_list|,
name|fix
argument_list|,
name|fix
argument_list|,
name|fix
argument_list|,
name|table
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HBaseFsck
name|doFsck
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|fixAssignments
parameter_list|,
name|boolean
name|fixMeta
parameter_list|,
name|boolean
name|fixHdfsHoles
parameter_list|,
name|boolean
name|fixHdfsOverlaps
parameter_list|,
name|boolean
name|fixHdfsOrphans
parameter_list|,
name|boolean
name|fixTableOrphans
parameter_list|,
name|boolean
name|fixVersionFile
parameter_list|,
name|boolean
name|fixReferenceFiles
parameter_list|,
name|boolean
name|fixEmptyMetaRegionInfo
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|Exception
block|{
name|HBaseFsck
name|fsck
init|=
operator|new
name|HBaseFsck
argument_list|(
name|conf
argument_list|,
name|exec
argument_list|)
decl_stmt|;
name|fsck
operator|.
name|connect
argument_list|()
expr_stmt|;
name|fsck
operator|.
name|setDisplayFullReport
argument_list|()
expr_stmt|;
comment|// i.e. -details
name|fsck
operator|.
name|setTimeLag
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixAssignments
argument_list|(
name|fixAssignments
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixMeta
argument_list|(
name|fixMeta
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixHdfsHoles
argument_list|(
name|fixHdfsHoles
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixHdfsOverlaps
argument_list|(
name|fixHdfsOverlaps
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixHdfsOrphans
argument_list|(
name|fixHdfsOrphans
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixTableOrphans
argument_list|(
name|fixTableOrphans
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixVersionFile
argument_list|(
name|fixVersionFile
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixReferenceFiles
argument_list|(
name|fixReferenceFiles
argument_list|)
expr_stmt|;
name|fsck
operator|.
name|setFixEmptyMetaCells
argument_list|(
name|fixEmptyMetaRegionInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|fsck
operator|.
name|includeTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|fsck
operator|.
name|onlineHbck
argument_list|()
expr_stmt|;
return|return
name|fsck
return|;
block|}
comment|/**    * Runs hbck with the -sidelineCorruptHFiles option    * @param conf    * @param table table constraint    * @return<returncode, hbckInstance>    * @throws Exception    */
specifier|public
specifier|static
name|HBaseFsck
name|doHFileQuarantine
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|args
init|=
block|{
literal|"-sidelineCorruptHFiles"
block|,
literal|"-ignorePreCheckPermission"
block|,
name|table
block|}
decl_stmt|;
name|HBaseFsck
name|hbck
init|=
operator|new
name|HBaseFsck
argument_list|(
name|conf
argument_list|,
name|exec
argument_list|)
decl_stmt|;
name|hbck
operator|.
name|exec
argument_list|(
name|exec
argument_list|,
name|args
argument_list|)
expr_stmt|;
return|return
name|hbck
return|;
block|}
specifier|public
specifier|static
name|void
name|assertNoErrors
parameter_list|(
name|HBaseFsck
name|fsck
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ERROR_CODE
argument_list|>
name|errs
init|=
name|fsck
operator|.
name|getErrors
argument_list|()
operator|.
name|getErrorList
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ERROR_CODE
argument_list|>
argument_list|()
argument_list|,
name|errs
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertErrors
parameter_list|(
name|HBaseFsck
name|fsck
parameter_list|,
name|ERROR_CODE
index|[]
name|expectedErrors
parameter_list|)
block|{
name|List
argument_list|<
name|ERROR_CODE
argument_list|>
name|errs
init|=
name|fsck
operator|.
name|getErrors
argument_list|()
operator|.
name|getErrorList
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|expectedErrors
argument_list|)
argument_list|,
name|errs
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

