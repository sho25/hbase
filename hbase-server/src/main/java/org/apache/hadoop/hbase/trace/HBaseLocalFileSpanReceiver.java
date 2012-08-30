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
name|trace
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
name|conf
operator|.
name|Configurable
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
name|cloudera
operator|.
name|htrace
operator|.
name|Span
import|;
end_import

begin_import
import|import
name|org
operator|.
name|cloudera
operator|.
name|htrace
operator|.
name|SpanReceiver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|cloudera
operator|.
name|htrace
operator|.
name|Trace
import|;
end_import

begin_import
import|import
name|org
operator|.
name|cloudera
operator|.
name|htrace
operator|.
name|impl
operator|.
name|LocalFileSpanReceiver
import|;
end_import

begin_comment
comment|/**  * Wraps the LocalFileSpanReceiver provided in  * org.cloudera.htrace.impl.LocalFileSpanReceiver to read the file name  * destination for spans from hbase-site.xml.  *   * The file path should be added as a property with name  * "hbase.trace.spanreceiver.localfilespanreceiver.filename".  */
end_comment

begin_class
specifier|public
class|class
name|HBaseLocalFileSpanReceiver
implements|implements
name|SpanReceiver
implements|,
name|Configurable
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
name|HBaseLocalFileSpanReceiver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FILE_NAME_CONF_KEY
init|=
literal|"hbase.trace.spanreceiver.localfilespanreceiver.filename"
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|LocalFileSpanReceiver
name|rcvr
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|arg0
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|arg0
expr_stmt|;
comment|// replace rcvr if it was already created
if|if
condition|(
name|rcvr
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|rcvr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error closing LocalFileSpanReceiver."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|rcvr
operator|=
operator|new
name|LocalFileSpanReceiver
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|FILE_NAME_CONF_KEY
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Trace
operator|.
name|removeReceiver
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|rcvr
operator|=
literal|null
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to initialize LocalFileSpanReceiver, removing owner (HBaseLocalFileSpanReceiver) from receiver list."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|rcvr
operator|!=
literal|null
condition|)
block|{
name|rcvr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|rcvr
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|receiveSpan
parameter_list|(
name|Span
name|span
parameter_list|)
block|{
if|if
condition|(
name|rcvr
operator|!=
literal|null
condition|)
block|{
name|rcvr
operator|.
name|receiveSpan
argument_list|(
name|span
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

