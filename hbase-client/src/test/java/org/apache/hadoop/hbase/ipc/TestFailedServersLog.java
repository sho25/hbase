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
name|ipc
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
import|;
end_import

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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertThat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|HBaseClassTestRule
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
name|testclassification
operator|.
name|ClientTests
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
name|testclassification
operator|.
name|SmallTests
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
name|Level
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
name|LogManager
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
name|spi
operator|.
name|LoggingEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|ArgumentCaptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Captor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|runners
operator|.
name|MockitoJUnitRunner
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestFailedServersLog
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestFailedServersLog
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|int
name|TEST_PORT
init|=
literal|9999
decl_stmt|;
specifier|private
name|InetSocketAddress
name|addr
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Appender
name|mockAppender
decl_stmt|;
annotation|@
name|Captor
specifier|private
name|ArgumentCaptor
name|captorLoggingEvent
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|LogManager
operator|.
name|getRootLogger
argument_list|()
operator|.
name|addAppender
argument_list|(
name|mockAppender
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
block|{
name|LogManager
operator|.
name|getRootLogger
argument_list|()
operator|.
name|removeAppender
argument_list|(
name|mockAppender
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddToFailedServersLogging
parameter_list|()
block|{
name|Throwable
name|nullException
init|=
operator|new
name|NullPointerException
argument_list|()
decl_stmt|;
name|FailedServers
name|fs
init|=
operator|new
name|FailedServers
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|addr
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|TEST_PORT
argument_list|)
expr_stmt|;
name|fs
operator|.
name|addToFailedServers
argument_list|(
name|addr
argument_list|,
name|nullException
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|mockAppender
argument_list|)
operator|.
name|doAppend
argument_list|(
operator|(
name|LoggingEvent
operator|)
name|captorLoggingEvent
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|LoggingEvent
name|loggingEvent
init|=
operator|(
name|LoggingEvent
operator|)
name|captorLoggingEvent
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|loggingEvent
operator|.
name|getLevel
argument_list|()
argument_list|,
name|is
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Added failed server with address "
operator|+
name|addr
operator|.
name|toString
argument_list|()
operator|+
literal|" to list caused by "
operator|+
name|nullException
operator|.
name|toString
argument_list|()
argument_list|,
name|loggingEvent
operator|.
name|getRenderedMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

