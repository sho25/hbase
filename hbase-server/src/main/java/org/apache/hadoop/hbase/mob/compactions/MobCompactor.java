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
name|mob
operator|.
name|compactions
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
name|FileStatus
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
name|TableName
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|mob
operator|.
name|MobUtils
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

begin_comment
comment|/**  * A mob compactor to directly compact the mob files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|MobCompactor
block|{
specifier|protected
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|TableName
name|tableName
decl_stmt|;
specifier|protected
name|ColumnFamilyDescriptor
name|column
decl_stmt|;
specifier|protected
name|Path
name|mobTableDir
decl_stmt|;
specifier|protected
name|Path
name|mobFamilyDir
decl_stmt|;
specifier|protected
name|ExecutorService
name|pool
decl_stmt|;
specifier|public
name|MobCompactor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|ColumnFamilyDescriptor
name|column
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|mobTableDir
operator|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|MobUtils
operator|.
name|getMobHome
argument_list|(
name|conf
argument_list|)
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|mobFamilyDir
operator|=
name|MobUtils
operator|.
name|getMobFamilyPath
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|,
name|column
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Compacts the mob files for the current column family.    * @return The paths of new mob files generated in the compaction.    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|compact
argument_list|(
literal|false
argument_list|)
return|;
block|}
comment|/**    * Compacts the mob files by compaction type for the current column family.    * @param allFiles Whether add all mob files into the compaction.    * @return The paths of new mob files generated in the compaction.    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
name|boolean
name|allFiles
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|compact
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|fs
operator|.
name|listStatus
argument_list|(
name|mobFamilyDir
argument_list|)
argument_list|)
argument_list|,
name|allFiles
argument_list|)
return|;
block|}
comment|/**    * Compacts the candidate mob files.    * @param files The candidate mob files.    * @param allFiles Whether add all mob files into the compaction.    * @return The paths of new mob files generated in the compaction.    * @throws IOException    */
specifier|public
specifier|abstract
name|List
argument_list|<
name|Path
argument_list|>
name|compact
parameter_list|(
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|,
name|boolean
name|allFiles
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

