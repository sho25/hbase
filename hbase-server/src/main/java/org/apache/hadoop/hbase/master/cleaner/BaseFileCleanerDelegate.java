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
name|cleaner
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
name|hbase
operator|.
name|BaseConfigurable
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
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

begin_comment
comment|/**  * Base class for file cleaners which allows subclasses to implement a simple  * isFileDeletable method (which used to be the FileCleanerDelegate contract).  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|BaseFileCleanerDelegate
extends|extends
name|BaseConfigurable
implements|implements
name|FileCleanerDelegate
block|{
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|getDeletableFiles
parameter_list|(
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
block|{
return|return
name|Iterables
operator|.
name|filter
argument_list|(
name|files
argument_list|,
operator|new
name|Predicate
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|FileStatus
name|file
parameter_list|)
block|{
return|return
name|isFileDeletable
argument_list|(
name|file
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
comment|// subclass could override it if needed.
block|}
comment|/**    * Should the master delete the file or keep it?    * @param fStat file status of the file to check    * @return<tt>true</tt> if the file is deletable,<tt>false</tt> if not    */
specifier|protected
specifier|abstract
name|boolean
name|isFileDeletable
parameter_list|(
name|FileStatus
name|fStat
parameter_list|)
function_decl|;
block|}
end_class

end_unit

