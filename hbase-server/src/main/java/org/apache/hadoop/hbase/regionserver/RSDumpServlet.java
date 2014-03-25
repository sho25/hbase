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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RSDumpServlet
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
name|HRegionServer
name|hrs
init|=
operator|(
name|HRegionServer
operator|)
name|getServletContext
argument_list|()
operator|.
name|getAttribute
argument_list|(
name|HRegionServer
operator|.
name|REGIONSERVER
argument_list|)
decl_stmt|;
assert|assert
name|hrs
operator|!=
literal|null
operator|:
literal|"No RS in context!"
assert|;
name|response
operator|.
name|setContentType
argument_list|(
literal|"text/plain"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|hrs
operator|.
name|isOnline
argument_list|()
condition|)
block|{
name|response
operator|.
name|getWriter
argument_list|()
operator|.
name|write
argument_list|(
literal|"The RegionServer is initializing!"
argument_list|)
expr_stmt|;
name|response
operator|.
name|getWriter
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
return|return;
block|}
name|OutputStream
name|os
init|=
name|response
operator|.
name|getOutputStream
argument_list|()
decl_stmt|;
name|PrintWriter
name|out
init|=
operator|new
name|PrintWriter
argument_list|(
name|os
argument_list|)
decl_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"RegionServer status for "
operator|+
name|hrs
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
name|hrs
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
name|ReflectionUtils
operator|.
name|printThreadInfo
argument_list|(
name|out
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\n\nRS Configuration:"
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
name|hrs
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
name|dumpQueue
argument_list|(
name|hrs
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
specifier|public
specifier|static
name|void
name|dumpQueue
parameter_list|(
name|HRegionServer
name|hrs
parameter_list|,
name|PrintWriter
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// 1. Print out Compaction/Split Queue
name|out
operator|.
name|println
argument_list|(
literal|"Compaction/Split Queue summary: "
operator|+
name|hrs
operator|.
name|compactSplitThread
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|hrs
operator|.
name|compactSplitThread
operator|.
name|dumpQueue
argument_list|()
argument_list|)
expr_stmt|;
comment|// 2. Print out flush Queue
name|out
operator|.
name|println
argument_list|(
literal|"\nFlush Queue summary: "
operator|+
name|hrs
operator|.
name|cacheFlusher
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|hrs
operator|.
name|cacheFlusher
operator|.
name|dumpQueue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

