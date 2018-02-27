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
name|master
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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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
name|Date
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
name|HttpServletRequest
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
name|HttpServletResponse
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
name|ServerMetrics
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentManager
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
name|assignment
operator|.
name|RegionStates
operator|.
name|RegionStateNode
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
name|monitoring
operator|.
name|LogMonitoring
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
name|monitoring
operator|.
name|StateDumpServlet
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
name|monitoring
operator|.
name|TaskMonitor
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
name|regionserver
operator|.
name|RSDumpServlet
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
name|Threads
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MasterDumpServlet
extends|extends
name|StateDumpServlet
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LINE
init|=
literal|"==========================================================="
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|HMaster
name|master
init|=
operator|(
name|HMaster
operator|)
name|getServletContext
argument_list|()
operator|.
name|getAttribute
argument_list|(
name|HMaster
operator|.
name|MASTER
argument_list|)
decl_stmt|;
assert|assert
name|master
operator|!=
literal|null
operator|:
literal|"No Master in context!"
assert|;
name|response
operator|.
name|setContentType
argument_list|(
literal|"text/plain"
argument_list|)
expr_stmt|;
name|OutputStream
name|os
init|=
name|response
operator|.
name|getOutputStream
argument_list|()
decl_stmt|;
try|try
init|(
name|PrintWriter
name|out
init|=
operator|new
name|PrintWriter
argument_list|(
name|os
argument_list|)
init|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"Master status for "
operator|+
name|master
operator|.
name|getServerName
argument_list|()
operator|+
literal|" as of "
operator|+
operator|new
name|Date
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nVersion Info:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|dumpVersionInfo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nTasks:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|dumpAsText
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nServers:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|dumpServers
argument_list|(
name|master
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nRegions-in-transition:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|dumpRIT
argument_list|(
name|master
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nExecutors:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|dumpExecutors
argument_list|(
name|master
operator|.
name|getExecutorService
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nStacks:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|PrintStream
name|ps
init|=
operator|new
name|PrintStream
argument_list|(
name|response
operator|.
name|getOutputStream
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|Threads
operator|.
name|printThreadInfo
argument_list|(
name|ps
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|ps
operator|.
name|flush
argument_list|()
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nMaster configuration:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|master
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|conf
operator|.
name|writeXml
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|os
operator|.
name|flush
argument_list|()
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nRecent regionserver aborts:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|master
operator|.
name|getRegionServerFatalLogBuffer
argument_list|()
operator|.
name|dumpTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nLogs"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
name|long
name|tailKb
init|=
name|getTailKbParam
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|LogMonitoring
operator|.
name|dumpTailOfLogs
argument_list|(
name|out
argument_list|,
name|tailKb
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nRS Queue:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|LINE
argument_list|)
expr_stmt|;
if|if
condition|(
name|isShowQueueDump
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|RSDumpServlet
operator|.
name|dumpQueue
argument_list|(
name|master
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|dumpRIT
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|PrintWriter
name|out
parameter_list|)
block|{
name|AssignmentManager
name|am
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|am
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"AssignmentManager is not initialized"
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|RegionStateNode
name|rs
range|:
name|am
operator|.
name|getRegionsInTransition
argument_list|()
control|)
block|{
name|String
name|rid
init|=
name|rs
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"Region "
operator|+
name|rid
operator|+
literal|": "
operator|+
name|rs
operator|.
name|toDescriptiveString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|dumpServers
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|PrintWriter
name|out
parameter_list|)
block|{
name|ServerManager
name|sm
init|=
name|master
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
literal|"ServerManager is not initialized"
argument_list|)
expr_stmt|;
return|return;
block|}
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|servers
init|=
name|sm
operator|.
name|getOnlineServers
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|e
range|:
name|servers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|println
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|": "
operator|+
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

