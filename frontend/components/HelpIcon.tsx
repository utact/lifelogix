'use client'

import Image from 'next/image';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip';

const HelpIcon = () => {
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <div className="fixed bottom-8 right-8 z-50 animate-pop-in transition-transform duration-200 ease-in-out hover:scale-110">
            <Image src="/kumdori.png" alt="Help" width={80} height={80} />
          </div>
        </TooltipTrigger>
        <TooltipContent>
          <p>이용방법이 궁금하신가요?</p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
};

export default HelpIcon;