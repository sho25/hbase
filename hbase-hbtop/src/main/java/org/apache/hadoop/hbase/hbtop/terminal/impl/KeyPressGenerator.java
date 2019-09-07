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
name|hbtop
operator|.
name|terminal
operator|.
name|impl
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|BlockingQueue
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
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
name|LinkedBlockingQueue
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
name|TimeUnit
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
name|atomic
operator|.
name|AtomicBoolean
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
name|hbtop
operator|.
name|terminal
operator|.
name|KeyPress
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|/**  * This generates {@link KeyPress} objects from the given input stream and offers them to the  * given queue.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|KeyPressGenerator
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|KeyPressGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
enum|enum
name|ParseState
block|{
name|START
block|,
name|ESCAPE
block|,
name|ESCAPE_SEQUENCE_PARAM1
block|,
name|ESCAPE_SEQUENCE_PARAM2
block|}
specifier|private
specifier|final
name|Queue
argument_list|<
name|KeyPress
argument_list|>
name|keyPressQueue
decl_stmt|;
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|Character
argument_list|>
name|inputCharacterQueue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Reader
name|input
decl_stmt|;
specifier|private
specifier|final
name|InputStream
name|inputStream
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|stopThreads
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|executorService
decl_stmt|;
specifier|private
name|ParseState
name|parseState
decl_stmt|;
specifier|private
name|int
name|param1
decl_stmt|;
specifier|private
name|int
name|param2
decl_stmt|;
specifier|public
name|KeyPressGenerator
parameter_list|(
name|InputStream
name|inputStream
parameter_list|,
name|Queue
argument_list|<
name|KeyPress
argument_list|>
name|keyPressQueue
parameter_list|)
block|{
name|this
operator|.
name|inputStream
operator|=
name|inputStream
expr_stmt|;
name|input
operator|=
operator|new
name|InputStreamReader
argument_list|(
name|inputStream
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyPressQueue
operator|=
name|keyPressQueue
expr_stmt|;
name|executorService
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|2
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
literal|"KeyPressGenerator-%d"
argument_list|)
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setUncaughtExceptionHandler
argument_list|(
name|Threads
operator|.
name|LOGGING_EXCEPTION_HANDLER
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|initState
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
name|executorService
operator|.
name|submit
argument_list|(
name|this
operator|::
name|readerThread
argument_list|)
expr_stmt|;
name|executorService
operator|.
name|submit
argument_list|(
name|this
operator|::
name|generatorThread
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initState
parameter_list|()
block|{
name|parseState
operator|=
name|ParseState
operator|.
name|START
expr_stmt|;
name|param1
operator|=
literal|0
expr_stmt|;
name|param2
operator|=
literal|0
expr_stmt|;
block|}
specifier|private
name|void
name|readerThread
parameter_list|()
block|{
name|boolean
name|done
init|=
literal|false
decl_stmt|;
name|char
index|[]
name|readBuffer
init|=
operator|new
name|char
index|[
literal|128
index|]
decl_stmt|;
while|while
condition|(
operator|!
name|done
operator|&&
operator|!
name|stopThreads
operator|.
name|get
argument_list|()
condition|)
block|{
try|try
block|{
name|int
name|n
init|=
name|inputStream
operator|.
name|available
argument_list|()
decl_stmt|;
if|if
condition|(
name|n
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|readBuffer
operator|.
name|length
operator|<
name|n
condition|)
block|{
name|readBuffer
operator|=
operator|new
name|char
index|[
name|readBuffer
operator|.
name|length
operator|*
literal|2
index|]
expr_stmt|;
block|}
name|int
name|rc
init|=
name|input
operator|.
name|read
argument_list|(
name|readBuffer
argument_list|,
literal|0
argument_list|,
name|readBuffer
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|rc
operator|==
operator|-
literal|1
condition|)
block|{
comment|// EOF
name|done
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rc
condition|;
name|i
operator|++
control|)
block|{
name|int
name|ch
init|=
name|readBuffer
index|[
name|i
index|]
decl_stmt|;
name|inputCharacterQueue
operator|.
name|offer
argument_list|(
operator|(
name|char
operator|)
name|ch
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|20
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignored
parameter_list|)
block|{       }
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Caught an exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|generatorThread
parameter_list|()
block|{
while|while
condition|(
operator|!
name|stopThreads
operator|.
name|get
argument_list|()
condition|)
block|{
name|Character
name|ch
decl_stmt|;
try|try
block|{
name|ch
operator|=
name|inputCharacterQueue
operator|.
name|poll
argument_list|(
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignored
parameter_list|)
block|{
continue|continue;
block|}
if|if
condition|(
name|ch
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|parseState
operator|==
name|ParseState
operator|.
name|ESCAPE
condition|)
block|{
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Escape
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|initState
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseState
operator|!=
name|ParseState
operator|.
name|START
condition|)
block|{
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Unknown
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|initState
argument_list|()
expr_stmt|;
block|}
continue|continue;
block|}
if|if
condition|(
name|parseState
operator|==
name|ParseState
operator|.
name|START
condition|)
block|{
if|if
condition|(
name|ch
operator|==
literal|0x1B
condition|)
block|{
name|parseState
operator|=
name|ParseState
operator|.
name|ESCAPE
expr_stmt|;
continue|continue;
block|}
switch|switch
condition|(
name|ch
condition|)
block|{
case|case
literal|'\n'
case|:
case|case
literal|'\r'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Enter
argument_list|,
literal|'\n'
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
case|case
literal|0x08
case|:
case|case
literal|0x7F
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Backspace
argument_list|,
literal|'\b'
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
case|case
literal|'\t'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Tab
argument_list|,
literal|'\t'
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|ch
operator|<
literal|32
condition|)
block|{
name|ctrlAndCharacter
argument_list|(
name|ch
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|isPrintableChar
argument_list|(
name|ch
argument_list|)
condition|)
block|{
comment|// Normal character
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Character
argument_list|,
name|ch
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Unknown
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|parseState
operator|==
name|ParseState
operator|.
name|ESCAPE
condition|)
block|{
if|if
condition|(
name|ch
operator|==
literal|0x1B
condition|)
block|{
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Escape
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|ch
operator|<
literal|32
operator|&&
name|ch
operator|!=
literal|0x08
condition|)
block|{
name|ctrlAltAndCharacter
argument_list|(
name|ch
argument_list|)
expr_stmt|;
name|initState
argument_list|()
expr_stmt|;
continue|continue;
block|}
elseif|else
if|if
condition|(
name|ch
operator|==
literal|0x7F
operator|||
name|ch
operator|==
literal|0x08
condition|)
block|{
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Backspace
argument_list|,
literal|'\b'
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|initState
argument_list|()
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|ch
operator|==
literal|'['
operator|||
name|ch
operator|==
literal|'O'
condition|)
block|{
name|parseState
operator|=
name|ParseState
operator|.
name|ESCAPE_SEQUENCE_PARAM1
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|isPrintableChar
argument_list|(
name|ch
argument_list|)
condition|)
block|{
comment|// Alt and character
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Character
argument_list|,
name|ch
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|initState
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Escape
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Unknown
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|initState
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|escapeSequenceCharacter
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|ctrlAndCharacter
parameter_list|(
name|char
name|ch
parameter_list|)
block|{
name|char
name|ctrlCode
decl_stmt|;
switch|switch
condition|(
name|ch
condition|)
block|{
case|case
literal|0
case|:
name|ctrlCode
operator|=
literal|' '
expr_stmt|;
break|break;
case|case
literal|28
case|:
name|ctrlCode
operator|=
literal|'\\'
expr_stmt|;
break|break;
case|case
literal|29
case|:
name|ctrlCode
operator|=
literal|']'
expr_stmt|;
break|break;
case|case
literal|30
case|:
name|ctrlCode
operator|=
literal|'^'
expr_stmt|;
break|break;
case|case
literal|31
case|:
name|ctrlCode
operator|=
literal|'_'
expr_stmt|;
break|break;
default|default:
name|ctrlCode
operator|=
call|(
name|char
call|)
argument_list|(
literal|'a'
operator|-
literal|1
operator|+
name|ch
argument_list|)
expr_stmt|;
break|break;
block|}
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Character
argument_list|,
name|ctrlCode
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isPrintableChar
parameter_list|(
name|char
name|ch
parameter_list|)
block|{
if|if
condition|(
name|Character
operator|.
name|isISOControl
argument_list|(
name|ch
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Character
operator|.
name|UnicodeBlock
name|block
init|=
name|Character
operator|.
name|UnicodeBlock
operator|.
name|of
argument_list|(
name|ch
argument_list|)
decl_stmt|;
return|return
name|block
operator|!=
literal|null
operator|&&
name|block
operator|!=
name|Character
operator|.
name|UnicodeBlock
operator|.
name|SPECIALS
return|;
block|}
specifier|private
name|void
name|ctrlAltAndCharacter
parameter_list|(
name|char
name|ch
parameter_list|)
block|{
name|char
name|ctrlCode
decl_stmt|;
switch|switch
condition|(
name|ch
condition|)
block|{
case|case
literal|0
case|:
name|ctrlCode
operator|=
literal|' '
expr_stmt|;
break|break;
case|case
literal|28
case|:
name|ctrlCode
operator|=
literal|'\\'
expr_stmt|;
break|break;
case|case
literal|29
case|:
name|ctrlCode
operator|=
literal|']'
expr_stmt|;
break|break;
case|case
literal|30
case|:
name|ctrlCode
operator|=
literal|'^'
expr_stmt|;
break|break;
case|case
literal|31
case|:
name|ctrlCode
operator|=
literal|'_'
expr_stmt|;
break|break;
default|default:
name|ctrlCode
operator|=
call|(
name|char
call|)
argument_list|(
literal|'a'
operator|-
literal|1
operator|+
name|ch
argument_list|)
expr_stmt|;
break|break;
block|}
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Character
argument_list|,
name|ctrlCode
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|escapeSequenceCharacter
parameter_list|(
name|char
name|ch
parameter_list|)
block|{
switch|switch
condition|(
name|parseState
condition|)
block|{
case|case
name|ESCAPE_SEQUENCE_PARAM1
case|:
if|if
condition|(
name|ch
operator|==
literal|';'
condition|)
block|{
name|parseState
operator|=
name|ParseState
operator|.
name|ESCAPE_SEQUENCE_PARAM2
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Character
operator|.
name|isDigit
argument_list|(
name|ch
argument_list|)
condition|)
block|{
name|param1
operator|=
name|param1
operator|*
literal|10
operator|+
name|Character
operator|.
name|digit
argument_list|(
name|ch
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|doneEscapeSequenceCharacter
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|ESCAPE_SEQUENCE_PARAM2
case|:
if|if
condition|(
name|Character
operator|.
name|isDigit
argument_list|(
name|ch
argument_list|)
condition|)
block|{
name|param2
operator|=
name|param2
operator|*
literal|10
operator|+
name|Character
operator|.
name|digit
argument_list|(
name|ch
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|doneEscapeSequenceCharacter
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
specifier|private
name|void
name|doneEscapeSequenceCharacter
parameter_list|(
name|char
name|last
parameter_list|)
block|{
name|boolean
name|alt
init|=
literal|false
decl_stmt|;
name|boolean
name|ctrl
init|=
literal|false
decl_stmt|;
name|boolean
name|shift
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|param2
operator|!=
literal|0
condition|)
block|{
name|alt
operator|=
name|isAlt
argument_list|(
name|param2
argument_list|)
expr_stmt|;
name|ctrl
operator|=
name|isCtrl
argument_list|(
name|param2
argument_list|)
expr_stmt|;
name|shift
operator|=
name|isShift
argument_list|(
name|param2
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|last
operator|!=
literal|'~'
condition|)
block|{
switch|switch
condition|(
name|last
condition|)
block|{
case|case
literal|'A'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|ArrowUp
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'B'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|ArrowDown
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'C'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|ArrowRight
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'D'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|ArrowLeft
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'H'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Home
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'F'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|End
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'P'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F1
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'Q'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F2
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'R'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F3
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'S'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F4
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'Z'
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|ReverseTab
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Unknown
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
name|initState
argument_list|()
expr_stmt|;
return|return;
block|}
switch|switch
condition|(
name|param1
condition|)
block|{
case|case
literal|1
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Home
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Insert
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Delete
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|End
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|PageUp
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|6
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|PageDown
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|11
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F1
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|12
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F2
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|13
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F3
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|14
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F4
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|15
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F5
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|17
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F6
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|18
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F7
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|19
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F8
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|20
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F9
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|21
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F10
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|23
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F11
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|24
case|:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|F12
argument_list|,
literal|null
argument_list|,
name|alt
argument_list|,
name|ctrl
argument_list|,
name|shift
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
name|offer
argument_list|(
operator|new
name|KeyPress
argument_list|(
name|KeyPress
operator|.
name|Type
operator|.
name|Unknown
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
name|initState
argument_list|()
expr_stmt|;
block|}
specifier|private
name|boolean
name|isShift
parameter_list|(
name|int
name|param
parameter_list|)
block|{
return|return
operator|(
name|param
operator|&
literal|1
operator|)
operator|!=
literal|0
return|;
block|}
specifier|private
name|boolean
name|isAlt
parameter_list|(
name|int
name|param
parameter_list|)
block|{
return|return
operator|(
name|param
operator|&
literal|2
operator|)
operator|!=
literal|0
return|;
block|}
specifier|private
name|boolean
name|isCtrl
parameter_list|(
name|int
name|param
parameter_list|)
block|{
return|return
operator|(
name|param
operator|&
literal|4
operator|)
operator|!=
literal|0
return|;
block|}
specifier|private
name|void
name|offer
parameter_list|(
name|KeyPress
name|keyPress
parameter_list|)
block|{
comment|// Handle ctrl + c
if|if
condition|(
name|keyPress
operator|.
name|isCtrl
argument_list|()
operator|&&
name|keyPress
operator|.
name|getType
argument_list|()
operator|==
name|KeyPress
operator|.
name|Type
operator|.
name|Character
operator|&&
name|keyPress
operator|.
name|getCharacter
argument_list|()
operator|==
literal|'c'
condition|)
block|{
name|System
operator|.
name|exit
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|keyPressQueue
operator|.
name|offer
argument_list|(
name|keyPress
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|stopThreads
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
while|while
condition|(
operator|!
name|executorService
operator|.
name|awaitTermination
argument_list|(
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Waiting for thread-pool to terminate"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Interrupted while waiting for thread-pool termination"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

