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
name|http
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|impl
operator|.
name|Log4JLogger
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
name|LogConfigurationException
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
name|log4j
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|NCSARequestLog
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|RequestLog
import|;
end_import

begin_comment
comment|/**  * RequestLog object for use with Http  */
end_comment

begin_class
specifier|public
class|class
name|HttpRequestLog
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
name|HttpRequestLog
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serverToComponent
decl_stmt|;
static|static
block|{
name|serverToComponent
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|serverToComponent
operator|.
name|put
argument_list|(
literal|"master"
argument_list|,
literal|"master"
argument_list|)
expr_stmt|;
name|serverToComponent
operator|.
name|put
argument_list|(
literal|"region"
argument_list|,
literal|"regionserver"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|RequestLog
name|getRequestLog
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|String
name|lookup
init|=
name|serverToComponent
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|lookup
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|lookup
expr_stmt|;
block|}
name|String
name|loggerName
init|=
literal|"http.requests."
operator|+
name|name
decl_stmt|;
name|String
name|appenderName
init|=
name|name
operator|+
literal|"requestlog"
decl_stmt|;
name|Log
name|logger
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|loggerName
argument_list|)
decl_stmt|;
if|if
condition|(
name|logger
operator|instanceof
name|Log4JLogger
condition|)
block|{
name|Log4JLogger
name|httpLog4JLog
init|=
operator|(
name|Log4JLogger
operator|)
name|logger
decl_stmt|;
name|Logger
name|httpLogger
init|=
name|httpLog4JLog
operator|.
name|getLogger
argument_list|()
decl_stmt|;
name|Appender
name|appender
init|=
literal|null
decl_stmt|;
try|try
block|{
name|appender
operator|=
name|httpLogger
operator|.
name|getAppender
argument_list|(
name|appenderName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LogConfigurationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Http request log for "
operator|+
name|loggerName
operator|+
literal|" could not be created"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
if|if
condition|(
name|appender
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Http request log for "
operator|+
name|loggerName
operator|+
literal|" is not defined"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|appender
operator|instanceof
name|HttpRequestLogAppender
condition|)
block|{
name|HttpRequestLogAppender
name|requestLogAppender
init|=
operator|(
name|HttpRequestLogAppender
operator|)
name|appender
decl_stmt|;
name|NCSARequestLog
name|requestLog
init|=
operator|new
name|NCSARequestLog
argument_list|()
decl_stmt|;
name|requestLog
operator|.
name|setFilename
argument_list|(
name|requestLogAppender
operator|.
name|getFilename
argument_list|()
argument_list|)
expr_stmt|;
name|requestLog
operator|.
name|setRetainDays
argument_list|(
name|requestLogAppender
operator|.
name|getRetainDays
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|requestLog
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Jetty request log for "
operator|+
name|loggerName
operator|+
literal|" was of the wrong class"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Jetty request log can only be enabled using Log4j"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

