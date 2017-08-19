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
name|yetus
operator|.
name|audience
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
name|fs
operator|.
name|PathFilter
import|;
end_import

begin_import
import|import
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|CheckForNull
import|;
end_import

begin_comment
comment|/**  * Typical base class for file status filter.  Works more efficiently when  * filtering file statuses, otherwise implementation will need to lookup filestatus  * for the path which will be expensive.  */
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
specifier|abstract
class|class
name|AbstractFileStatusFilter
implements|implements
name|PathFilter
implements|,
name|FileStatusFilter
block|{
comment|/**    * Filters out a path.  Can be given an optional directory hint to avoid    * filestatus lookup.    *    * @param p       A filesystem path    * @param isDir   An optional boolean indicating whether the path is a directory or not    * @return        true if the path is accepted, false if the path is filtered out    */
specifier|protected
specifier|abstract
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|,
annotation|@
name|CheckForNull
name|Boolean
name|isDir
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|FileStatus
name|f
parameter_list|)
block|{
return|return
name|accept
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
argument_list|,
name|f
operator|.
name|isDirectory
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
return|return
name|accept
argument_list|(
name|p
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|protected
name|boolean
name|isFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
annotation|@
name|CheckForNull
name|Boolean
name|isDir
parameter_list|,
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|!
name|isDirectory
argument_list|(
name|fs
argument_list|,
name|isDir
argument_list|,
name|p
argument_list|)
return|;
block|}
specifier|protected
name|boolean
name|isDirectory
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
annotation|@
name|CheckForNull
name|Boolean
name|isDir
parameter_list|,
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|isDir
operator|!=
literal|null
condition|?
name|isDir
else|:
name|fs
operator|.
name|isDirectory
argument_list|(
name|p
argument_list|)
return|;
block|}
block|}
end_class

end_unit

