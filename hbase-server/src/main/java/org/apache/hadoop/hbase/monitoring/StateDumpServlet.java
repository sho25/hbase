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
name|monitoring
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
name|io
operator|.
name|PrintWriter
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
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServlet
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
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
name|hbase
operator|.
name|executor
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
name|hbase
operator|.
name|executor
operator|.
name|ExecutorService
operator|.
name|ExecutorStatus
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
name|VersionInfo
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|StateDumpServlet
extends|extends
name|HttpServlet
block|{
specifier|static
specifier|final
name|long
name|DEFAULT_TAIL_KB
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
name|void
name|dumpVersionInfo
parameter_list|(
name|PrintWriter
name|out
parameter_list|)
block|{
name|VersionInfo
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Hadoop "
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Subversion "
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getUrl
argument_list|()
operator|+
literal|" -r "
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getRevision
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Compiled by "
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getUser
argument_list|()
operator|+
literal|" on "
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|VersionInfo
operator|.
name|getDate
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|long
name|getTailKbParam
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|)
block|{
name|String
name|param
init|=
name|request
operator|.
name|getParameter
argument_list|(
literal|"tailkb"
argument_list|)
decl_stmt|;
if|if
condition|(
name|param
operator|==
literal|null
condition|)
block|{
return|return
name|DEFAULT_TAIL_KB
return|;
block|}
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|param
argument_list|)
return|;
block|}
specifier|protected
name|void
name|dumpExecutors
parameter_list|(
name|ExecutorService
name|service
parameter_list|,
name|PrintWriter
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|service
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"ExecutorService is not initialized"
argument_list|)
expr_stmt|;
return|return;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|ExecutorStatus
argument_list|>
name|statuses
init|=
name|service
operator|.
name|getAllExecutorStatuses
argument_list|()
decl_stmt|;
for|for
control|(
name|ExecutorStatus
name|status
range|:
name|statuses
operator|.
name|values
argument_list|()
control|)
block|{
name|status
operator|.
name|dumpTo
argument_list|(
name|out
argument_list|,
literal|"  "
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

