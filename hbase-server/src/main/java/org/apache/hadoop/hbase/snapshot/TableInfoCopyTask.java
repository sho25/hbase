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
name|snapshot
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|FSTableDescriptors
import|;
end_import

begin_comment
comment|/**  * Copy the table info into the snapshot directory  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|TableInfoCopyTask
extends|extends
name|SnapshotTask
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TableInfoCopyTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|rootDir
decl_stmt|;
comment|/**    * Copy the table info for the given table into the snapshot    * @param monitor listen for errors while running the snapshot    * @param snapshot snapshot for which we are copying the table info    * @param fs {@link FileSystem} where the tableinfo is stored (and where the copy will be written)    * @param rootDir root of the {@link FileSystem} where the tableinfo is stored    */
specifier|public
name|TableInfoCopyTask
parameter_list|(
name|ForeignExceptionDispatcher
name|monitor
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|)
block|{
name|super
argument_list|(
name|snapshot
argument_list|,
name|monitor
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootDir
operator|=
name|rootDir
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Running table info copy."
argument_list|)
expr_stmt|;
name|this
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Attempting to copy table info for snapshot:"
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|this
operator|.
name|snapshot
argument_list|)
argument_list|)
expr_stmt|;
comment|// get the HTable descriptor
name|HTableDescriptor
name|orig
init|=
name|FSTableDescriptors
operator|.
name|getTableDescriptorFromFs
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|this
operator|.
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
comment|// write a copy of descriptor to the snapshot directory
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getWorkingSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|)
operator|.
name|createTableDescriptorForTableDirectory
argument_list|(
name|snapshotDir
argument_list|,
name|orig
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished copying tableinfo."
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

